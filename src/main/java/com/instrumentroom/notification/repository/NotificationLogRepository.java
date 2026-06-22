package com.instrumentroom.notification.repository;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.NotificationType;
import com.instrumentroom.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Query("SELECT nl FROM NotificationLog nl WHERE " +
           "nl.userId = :userId AND " +
           "nl.relatedId = :relatedId AND " +
           "nl.messageType = :messageType AND " +
           "nl.channel = :channel AND " +
           "nl.isSuccess = true")
    Optional<NotificationLog> findSuccessfulNotification(
            @Param("userId") Long userId,
            @Param("relatedId") Long relatedId,
            @Param("messageType") NotificationType messageType,
            @Param("channel") NotificationChannel channel);

    @Query("SELECT COUNT(nl) > 0 FROM NotificationLog nl WHERE " +
           "nl.userId = :userId AND " +
           "nl.relatedId = :relatedId AND " +
           "nl.messageType = :messageType AND " +
           "nl.channel = :channel AND " +
           "nl.isSuccess = true AND " +
           "nl.createdAt >= :since")
    boolean hasSuccessfulNotificationSince(
            @Param("userId") Long userId,
            @Param("relatedId") Long relatedId,
            @Param("messageType") NotificationType messageType,
            @Param("channel") NotificationChannel channel,
            @Param("since") LocalDateTime since);
}
