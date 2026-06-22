package com.instrumentroom.notification.dto;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class NotificationRequest {
    private NotificationType type;
    private Long userId;
    private String email;
    private String phone;
    private Map<String, Object> variables;
    private NotificationChannel[] channels;
}
