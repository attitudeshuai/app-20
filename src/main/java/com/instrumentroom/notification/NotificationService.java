package com.instrumentroom.notification;

import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.entity.User;
import com.instrumentroom.notification.dto.NotificationRequest;
import com.instrumentroom.notification.dto.NotificationResult;
import com.instrumentroom.notification.sender.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<NotificationChannel, NotificationSender> senderMap = new EnumMap<>(NotificationChannel.class);

    public NotificationService(List<NotificationSender> senders) {
        for (NotificationSender sender : senders) {
            senderMap.put(sender.getChannel(), sender);
            logger.info("已注册通知发送器: {}", sender.getChannel());
        }
    }

    public List<NotificationResult> send(NotificationRequest request) {
        List<NotificationResult> results = new ArrayList<>();

        NotificationChannel[] channels = request.getChannels();
        if (channels == null || channels.length == 0) {
            channels = new NotificationChannel[]{
                    NotificationChannel.EMAIL,
                    NotificationChannel.SITE_MESSAGE
            };
        }

        for (NotificationChannel channel : channels) {
            NotificationSender sender = senderMap.get(channel);
            if (sender == null) {
                logger.warn("未找到发送器: {}", channel);
                results.add(NotificationResult.builder()
                        .channel(channel)
                        .success(false)
                        .message("未找到发送器")
                        .build());
                continue;
            }

            if (!sender.isAvailable()) {
                logger.debug("发送器不可用: {}, 跳过", channel);
                results.add(NotificationResult.builder()
                        .channel(channel)
                        .success(false)
                        .message("渠道不可用")
                        .build());
                continue;
            }

            try {
                NotificationResult result = sender.send(request);
                results.add(result);
            } catch (Exception e) {
                logger.error("发送通知异常，渠道: {}", channel, e);
                results.add(NotificationResult.builder()
                        .channel(channel)
                        .success(false)
                        .message("发送异常: " + e.getMessage())
                        .build());
            }
        }

        return results;
    }

    public void notifyBookingCreated(Booking booking) {
        User user = booking.getUser();
        PracticeRoom room = booking.getRoom();

        Map<String, Object> variables = buildBookingVariables(booking, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.BOOKING_CREATED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("预约创建通知已触发，预约ID: {}, 用户ID: {}", booking.getId(), user.getId());
    }

    public void notifyBookingCancelled(Booking booking, String reason) {
        User user = booking.getUser();
        PracticeRoom room = booking.getRoom();

        Map<String, Object> variables = buildBookingVariables(booking, user, room);
        variables.put("cancelReason", reason != null ? reason : "用户取消");

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.BOOKING_CANCELLED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("预约取消通知已触发，预约ID: {}, 用户ID: {}", booking.getId(), user.getId());
    }

    public void notifyBookingReminder(Booking booking) {
        User user = booking.getUser();
        PracticeRoom room = booking.getRoom();

        Map<String, Object> variables = buildBookingVariables(booking, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.BOOKING_REMINDER)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("预约开始提醒已触发，预约ID: {}, 用户ID: {}", booking.getId(), user.getId());
    }

    public void notifyCheckInOverdue(Booking booking) {
        User user = booking.getUser();
        PracticeRoom room = booking.getRoom();

        Map<String, Object> variables = buildBookingVariables(booking, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.CHECK_IN_OVERDUE)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("入住逾期提醒已触发，预约ID: {}, 用户ID: {}", booking.getId(), user.getId());
    }

    public void notifyBookingConfirmed(Booking booking) {
        User user = booking.getUser();
        PracticeRoom room = booking.getRoom();

        Map<String, Object> variables = buildBookingVariables(booking, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.BOOKING_CONFIRMED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("预约确认通知已触发，预约ID: {}, 用户ID: {}", booking.getId(), user.getId());
    }

    public void notifyBookingCompleted(Booking booking) {
        User user = booking.getUser();
        PracticeRoom room = booking.getRoom();

        Map<String, Object> variables = buildBookingVariables(booking, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.BOOKING_COMPLETED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("预约完成通知已触发，预约ID: {}, 用户ID: {}", booking.getId(), user.getId());
    }

    private Map<String, Object> buildBookingVariables(Booking booking, User user, PracticeRoom room) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("bookingId", booking.getId());
        variables.put("username", user.getUsername());
        variables.put("roomName", room.getName());
        variables.put("roomLocation", room.getLocation());
        variables.put("bookingDate", booking.getBookingDate().format(DATE_FORMATTER));
        variables.put("startTime", booking.getStartTime().format(TIME_FORMATTER));
        variables.put("endTime", booking.getEndTime().format(TIME_FORMATTER));
        variables.put("purpose", booking.getPurpose() != null ? booking.getPurpose() : "练习");
        return variables;
    }
}
