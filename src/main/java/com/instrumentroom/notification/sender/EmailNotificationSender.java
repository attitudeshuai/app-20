package com.instrumentroom.notification.sender;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.dto.NotificationRequest;
import com.instrumentroom.notification.dto.NotificationResult;
import com.instrumentroom.notification.dto.RenderedTemplate;
import com.instrumentroom.notification.template.NotificationTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final NotificationTemplateService templateService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@instrumentroom.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    public EmailNotificationSender(NotificationTemplateService templateService,
                                   JavaMailSender mailSender) {
        this.templateService = templateService;
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean isAvailable() {
        return emailEnabled && mailSender != null;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!isAvailable()) {
            return NotificationResult.builder()
                    .channel(NotificationChannel.EMAIL)
                    .success(false)
                    .message("邮件服务未启用")
                    .build();
        }

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return NotificationResult.builder()
                    .channel(NotificationChannel.EMAIL)
                    .success(false)
                    .message("收件人邮箱为空")
                    .build();
        }

        try {
            RenderedTemplate template = templateService.render(
                    request.getType(),
                    NotificationChannel.EMAIL,
                    request.getVariables()
            );

            if (template == null) {
                return NotificationResult.builder()
                        .channel(NotificationChannel.EMAIL)
                        .success(false)
                        .message("未找到邮件模板")
                        .build();
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getEmail());
            message.setSubject(template.getSubject());
            message.setText(template.getContent());

            mailSender.send(message);

            String messageId = UUID.randomUUID().toString();
            logger.info("邮件发送成功，收件人: {}, 消息ID: {}", request.getEmail(), messageId);

            return NotificationResult.builder()
                    .channel(NotificationChannel.EMAIL)
                    .success(true)
                    .messageId(messageId)
                    .message("发送成功")
                    .build();

        } catch (MailException e) {
            logger.error("邮件发送失败，收件人: {}, 错误: {}", request.getEmail(), e.getMessage());
            return NotificationResult.builder()
                    .channel(NotificationChannel.EMAIL)
                    .success(false)
                    .message("邮件发送失败: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("邮件发送异常，收件人: {}", request.getEmail(), e);
            return NotificationResult.builder()
                    .channel(NotificationChannel.EMAIL)
                    .success(false)
                    .message("邮件发送异常: " + e.getMessage())
                    .build();
        }
    }
}
