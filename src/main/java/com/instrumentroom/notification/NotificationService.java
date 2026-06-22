package com.instrumentroom.notification;

import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.entity.User;
import com.instrumentroom.entity.Waitlist;
import com.instrumentroom.notification.dto.NotificationRequest;
import com.instrumentroom.notification.dto.NotificationResult;
import com.instrumentroom.notification.sender.NotificationSender;
import com.instrumentroom.notification.service.NotificationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final long DEFAULT_DEDUP_HOURS = 24;

    private final Map<NotificationChannel, NotificationSender> senderMap = new EnumMap<>(NotificationChannel.class);
    private final NotificationLogService notificationLogService;

    public NotificationService(List<NotificationSender> senders,
                               NotificationLogService notificationLogService) {
        for (NotificationSender sender : senders) {
            senderMap.put(sender.getChannel(), sender);
            logger.info("已注册通知发送器: {}", sender.getChannel());
        }
        this.notificationLogService = notificationLogService;
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

        Long relatedId = notificationLogService.extractRelatedId(request.getVariables());
        Long userId = request.getUserId();

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

            if (isDuplicate(userId, relatedId, request.getType(), channel)) {
                logger.debug("通知已发送过，跳过重复发送，用户ID: {}, 类型: {}, 渠道: {}, 关联ID: {}",
                        userId, request.getType(), channel, relatedId);
                results.add(NotificationResult.builder()
                        .channel(channel)
                        .success(true)
                        .message("已发送过，跳过重复")
                        .build());
                continue;
            }

            try {
                NotificationResult result = sender.send(request);
                results.add(result);
                notificationLogService.logResult(userId, relatedId, request.getType(), channel, result);
            } catch (Exception e) {
                logger.error("发送通知异常，渠道: {}", channel, e);
                NotificationResult errorResult = NotificationResult.builder()
                        .channel(channel)
                        .success(false)
                        .message("发送异常: " + e.getMessage())
                        .build();
                results.add(errorResult);
                notificationLogService.logResult(userId, relatedId, request.getType(), channel, errorResult);
            }
        }

        return results;
    }

    private boolean isDuplicate(Long userId, Long relatedId,
                                NotificationType type, NotificationChannel channel) {
        if (userId == null || relatedId == null) {
            return false;
        }
        if (type == NotificationType.BOOKING_REMINDER
                || type == NotificationType.CHECK_IN_OVERDUE) {
            LocalDateTime since = LocalDateTime.now().minusHours(DEFAULT_DEDUP_HOURS);
            return notificationLogService.hasSentWithin(userId, relatedId, type, channel, since);
        }
        return notificationLogService.hasSentSuccessfully(userId, relatedId, type, channel);
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

    public void notifyWaitlistCreated(Waitlist waitlist) {
        User user = waitlist.getUser();
        PracticeRoom room = waitlist.getRoom();

        Map<String, Object> variables = buildWaitlistVariables(waitlist, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.WAITLIST_CREATED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("候补创建通知已触发，候补ID: {}, 用户ID: {}", waitlist.getId(), user.getId());
    }

    public void notifyWaitlistPromoted(Waitlist waitlist, Booking booking) {
        User user = waitlist.getUser();
        PracticeRoom room = waitlist.getRoom();

        Map<String, Object> variables = buildWaitlistVariables(waitlist, user, room);
        variables.put("bookingId", booking.getId());

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.WAITLIST_PROMOTED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("候补转正通知已触发，候补ID: {}, 预约ID: {}, 用户ID: {}",
                waitlist.getId(), booking.getId(), user.getId());
    }

    public void notifyWaitlistCancelled(Waitlist waitlist, String reason) {
        User user = waitlist.getUser();
        PracticeRoom room = waitlist.getRoom();

        Map<String, Object> variables = buildWaitlistVariables(waitlist, user, room);
        variables.put("cancelReason", reason != null ? reason : "用户取消");

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.WAITLIST_CANCELLED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("候补取消通知已触发，候补ID: {}, 用户ID: {}", waitlist.getId(), user.getId());
    }

    public void notifyWaitlistExpired(Waitlist waitlist) {
        User user = waitlist.getUser();
        PracticeRoom room = waitlist.getRoom();

        Map<String, Object> variables = buildWaitlistVariables(waitlist, user, room);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.WAITLIST_EXPIRED)
                .userId(user.getId())
                .email(user.getEmail())
                .variables(variables)
                .build();

        send(request);
        logger.info("候补过期通知已触发，候补ID: {}, 用户ID: {}", waitlist.getId(), user.getId());
    }

    private Map<String, Object> buildWaitlistVariables(Waitlist waitlist, User user, PracticeRoom room) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("waitlistId", waitlist.getId());
        variables.put("username", user.getUsername());
        variables.put("roomName", room.getName());
        variables.put("roomLocation", room.getLocation());
        variables.put("bookingDate", waitlist.getBookingDate().format(DATE_FORMATTER));
        variables.put("startTime", waitlist.getStartTime().format(TIME_FORMATTER));
        variables.put("endTime", waitlist.getEndTime().format(TIME_FORMATTER));
        variables.put("priority", waitlist.getPriority());
        variables.put("queuePosition", waitlist.getQueuePosition() != null ? waitlist.getQueuePosition() : "-");
        variables.put("purpose", waitlist.getPurpose() != null ? waitlist.getPurpose() : "练习");
        return variables;
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
