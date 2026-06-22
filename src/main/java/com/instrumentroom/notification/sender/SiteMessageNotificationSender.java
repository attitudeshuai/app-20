package com.instrumentroom.notification.sender;

import com.instrumentroom.entity.User;
import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.dto.NotificationRequest;
import com.instrumentroom.notification.dto.NotificationResult;
import com.instrumentroom.notification.dto.RenderedTemplate;
import com.instrumentroom.notification.entity.SiteMessage;
import com.instrumentroom.notification.repository.SiteMessageRepository;
import com.instrumentroom.notification.template.NotificationTemplateService;
import com.instrumentroom.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SiteMessageNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(SiteMessageNotificationSender.class);

    private final NotificationTemplateService templateService;
    private final SiteMessageRepository siteMessageRepository;
    private final UserRepository userRepository;

    public SiteMessageNotificationSender(NotificationTemplateService templateService,
                                         SiteMessageRepository siteMessageRepository,
                                         UserRepository userRepository) {
        this.templateService = templateService;
        this.siteMessageRepository = siteMessageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SITE_MESSAGE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    @Transactional
    public NotificationResult send(NotificationRequest request) {
        if (request.getUserId() == null) {
            return NotificationResult.builder()
                    .channel(NotificationChannel.SITE_MESSAGE)
                    .success(false)
                    .message("用户ID为空")
                    .build();
        }

        try {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return NotificationResult.builder()
                        .channel(NotificationChannel.SITE_MESSAGE)
                        .success(false)
                        .message("用户不存在")
                        .build();
            }

            RenderedTemplate template = templateService.render(
                    request.getType(),
                    NotificationChannel.SITE_MESSAGE,
                    request.getVariables()
            );

            if (template == null) {
                return NotificationResult.builder()
                        .channel(NotificationChannel.SITE_MESSAGE)
                        .success(false)
                        .message("未找到站内信模板")
                        .build();
            }

            Long relatedId = request.getVariables() != null
                    ? (Long) request.getVariables().get("bookingId")
                    : null;

            boolean alreadySent = relatedId != null &&
                    siteMessageRepository.existsByUserIdAndRelatedIdAndMessageType(
                            request.getUserId(), relatedId, request.getType());

            if (alreadySent) {
                logger.debug("站内信已发送过，跳过重复发送，用户ID: {}, 类型: {}, 关联ID: {}",
                        request.getUserId(), request.getType(), relatedId);
                return NotificationResult.builder()
                        .channel(NotificationChannel.SITE_MESSAGE)
                        .success(true)
                        .message("已发送过，跳过重复")
                        .build();
            }

            SiteMessage siteMessage = SiteMessage.builder()
                    .user(user)
                    .title(template.getSubject())
                    .content(template.getContent())
                    .messageType(request.getType())
                    .relatedId(relatedId)
                    .isRead(false)
                    .build();

            siteMessage = siteMessageRepository.save(siteMessage);

            logger.info("站内信发送成功，用户ID: {}, 消息ID: {}", request.getUserId(), siteMessage.getId());

            return NotificationResult.builder()
                    .channel(NotificationChannel.SITE_MESSAGE)
                    .success(true)
                    .messageId(String.valueOf(siteMessage.getId()))
                    .message("发送成功")
                    .build();

        } catch (Exception e) {
            logger.error("站内信发送异常，用户ID: {}", request.getUserId(), e);
            return NotificationResult.builder()
                    .channel(NotificationChannel.SITE_MESSAGE)
                    .success(false)
                    .message("站内信发送异常: " + e.getMessage())
                    .build();
        }
    }
}
