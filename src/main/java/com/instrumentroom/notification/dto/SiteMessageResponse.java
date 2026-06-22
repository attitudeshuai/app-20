package com.instrumentroom.notification.dto;

import com.instrumentroom.notification.NotificationType;
import com.instrumentroom.notification.entity.SiteMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SiteMessageResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType messageType;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static SiteMessageResponse fromEntity(SiteMessage message) {
        return SiteMessageResponse.builder()
                .id(message.getId())
                .title(message.getTitle())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .relatedId(message.getRelatedId())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }
}
