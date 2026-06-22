package com.instrumentroom.notification.service;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.NotificationType;
import com.instrumentroom.notification.dto.NotificationResult;
import com.instrumentroom.notification.entity.NotificationLog;
import com.instrumentroom.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NotificationLogService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationLogService.class);

    private final NotificationLogRepository notificationLogRepository;

    public NotificationLogService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    public boolean hasSentSuccessfully(Long userId, Long relatedId,
                                       NotificationType type, NotificationChannel channel) {
        if (userId == null || relatedId == null) {
            return false;
        }
        return notificationLogRepository.findSuccessfulNotification(userId, relatedId, type, channel)
                .isPresent();
    }

    public boolean hasSentWithin(Long userId, Long relatedId,
                            NotificationType type, NotificationChannel channel,
                            LocalDateTime since) {
        if (userId == null || relatedId == null || since == null) {
            return false;
        }
        return notificationLogRepository.hasSuccessfulNotificationSince(
                userId, relatedId, type, channel, since);
    }

    @Transactional
    public void logResult(Long userId, Long relatedId,
                         NotificationType type, NotificationChannel channel,
                         NotificationResult result) {
        try {
            NotificationLog log = NotificationLog.builder()
                    .userId(userId)
                    .relatedId(relatedId)
                    .messageType(type)
                    .channel(channel)
                    .isSuccess(result.isSuccess())
                    .messageId(result.getMessageId())
                    .errorMessage(result.isSuccess() ? null : result.getMessage())
                    .build();
            notificationLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("记录通知日志失败，用户ID: {}, 类型: {}, 渠道: {}", userId, type, channel, e);
        }
    }

    public Long extractRelatedId(Map<String, Object> variables) {
        if (variables == null) {
            return null;
        }
        Object bookingId = variables.get("bookingId");
        return bookingId instanceof Long ? (Long) bookingId : null;
    }
}
