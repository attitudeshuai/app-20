package com.instrumentroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;

@Configuration
public class NotificationConfig {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConfig.class);

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        if (emailEnabled) {
            logger.info("邮件通知已启用，使用标准JavaMailSender");
            return new JavaMailSenderImpl();
        } else {
            logger.info("邮件通知未启用，使用NoopJavaMailSender降级实现");
            return new NoopJavaMailSender();
        }
    }

    private static class NoopJavaMailSender implements JavaMailSender {

        private static final Logger log = LoggerFactory.getLogger(NoopJavaMailSender.class);

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            log.debug("邮件服务未启用，模拟发送邮件到: {}", simpleMessage.getTo());
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            for (SimpleMailMessage message : simpleMessages) {
                send(message);
            }
        }

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage((jakarta.mail.Session) null);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            return createMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            try {
                log.debug("邮件服务未启用，模拟发送Mime邮件");
            } catch (Exception e) {
                log.debug("模拟邮件发送", e);
            }
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            for (MimeMessage message : mimeMessages) {
                send(message);
            }
        }
    }
}
