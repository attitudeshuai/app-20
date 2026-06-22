package com.instrumentroom.notification.entity;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(columnList = "user_id, related_id, message_type, channel"),
        @Index(columnList = "user_id, related_id, message_type, channel, createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 50, nullable = false)
    private NotificationType messageType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NotificationChannel channel;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
