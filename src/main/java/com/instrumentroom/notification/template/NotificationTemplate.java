package com.instrumentroom.notification.template;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationTemplate {
    private NotificationType type;
    private NotificationChannel channel;
    private String subjectTemplate;
    private String contentTemplate;
}
