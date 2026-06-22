package com.instrumentroom.notification.sender;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.dto.NotificationRequest;
import com.instrumentroom.notification.dto.NotificationResult;

public interface NotificationSender {
    NotificationChannel getChannel();
    NotificationResult send(NotificationRequest request);
    boolean isAvailable();
}
