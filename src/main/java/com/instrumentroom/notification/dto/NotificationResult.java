package com.instrumentroom.notification.dto;

import com.instrumentroom.notification.NotificationChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResult {
    private NotificationChannel channel;
    private boolean success;
    private String message;
    private String messageId;
}
