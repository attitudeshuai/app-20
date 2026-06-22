package com.instrumentroom.notification.sender;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.dto.NotificationRequest;
import com.instrumentroom.notification.dto.NotificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean isAvailable() {
        return smsEnabled;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        logger.warn("短信渠道暂未实现，用户ID: {}, 类型: {}", request.getUserId(), request.getType());
        return NotificationResult.builder()
                .channel(NotificationChannel.SMS)
                .success(false)
                .message("短信渠道暂未启用")
                .build();
    }
}
