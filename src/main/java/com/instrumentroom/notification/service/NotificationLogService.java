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
import java.util.Optional;
import java.util.Set;

@Service
public class NotificationLogService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationLogService.class);

    private static final Set<NotificationType> ONE_TIME_TYPES = Set.of(
            NotificationType.BOOKING_CREATED,
            NotificationType.BOOKING_CANCELLED,
            NotificationType.BOOKING_CONFIRMED,
            NotificationType.BOOKING_COMPLETED
    );

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
        Optional<LocalDateTime> lastSent = notificationLogRepository.findLastSentAt(
                userId, relatedId, type, channel);
        return lastSent.isPresent() && !lastSent.get().isBefore(since);
    }

    @Transactional
    public void logResult(Long userId, Long relatedId,
                         NotificationType type, NotificationChannel channel,
                         NotificationResult result) {
        if (!result.isSuccess()) {
            saveLog(userId, relatedId, type, channel, result);
            return;
        }

        if (ONE_TIME_TYPES.contains(type) && userId != null && relatedId != null) {
            if (notificationLogRepository.findSuccessfulNotification(userId, relatedId, type, channel).isPresent()) {
                logger.debug("一次性通知已存在成功记录，跳过写入，用户ID: {}, 类型: {}, 渠道: {}", userId, type, channel);
                return;
            }
        }

        saveLog(userId, relatedId, type, channel, result);
    }

    private void saveLog(Long userId, Long relatedId,
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
