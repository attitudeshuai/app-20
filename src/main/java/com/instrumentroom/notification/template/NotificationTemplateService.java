package com.instrumentroom.notification.template;

import com.instrumentroom.notification.NotificationChannel;
import com.instrumentroom.notification.NotificationType;
import com.instrumentroom.notification.dto.RenderedTemplate;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTemplateService.class);

    private final Map<String, NotificationTemplate> templateCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initTemplates() {
        registerTemplate(NotificationType.BOOKING_CREATED, NotificationChannel.EMAIL,
                "【乐器室】预约创建成功",
                "尊敬的{{username}}，您好：\n\n您的预约已创建成功！\n\n预约详情：\n- 预约编号：{{bookingId}}\n- 练习室：{{roomName}}\n- 位置：{{roomLocation}}\n- 日期：{{bookingDate}}\n- 时间：{{startTime}} - {{endTime}}\n- 用途：{{purpose}}\n\n请在预约开始前30分钟内办理签到。\n\n感谢您的使用！\n乐器室管理团队");

        registerTemplate(NotificationType.BOOKING_CREATED, NotificationChannel.SITE_MESSAGE,
                "预约创建成功",
                "您的预约已创建成功！练习室：{{roomName}}，时间：{{bookingDate}} {{startTime}}-{{endTime}}。请按时签到。");

        registerTemplate(NotificationType.BOOKING_CANCELLED, NotificationChannel.EMAIL,
                "【乐器室】预约已取消",
                "尊敬的{{username}}，您好：\n\n您的预约已取消。\n\n预约详情：\n- 预约编号：{{bookingId}}\n- 练习室：{{roomName}}\n- 日期：{{bookingDate}}\n- 时间：{{startTime}} - {{endTime}}\n\n取消原因：{{cancelReason}}\n\n如需重新预约，请登录系统操作。\n\n乐器室管理团队");

        registerTemplate(NotificationType.BOOKING_CANCELLED, NotificationChannel.SITE_MESSAGE,
                "预约已取消",
                "您的预约已取消。练习室：{{roomName}}，时间：{{bookingDate}} {{startTime}}-{{endTime}}。");

        registerTemplate(NotificationType.BOOKING_REMINDER, NotificationChannel.EMAIL,
                "【乐器室】预约开始提醒",
                "尊敬的{{username}}，您好：\n\n温馨提醒：您的预约即将开始！\n\n预约详情：\n- 预约编号：{{bookingId}}\n- 练习室：{{roomName}}\n- 位置：{{roomLocation}}\n- 日期：{{bookingDate}}\n- 时间：{{startTime}} - {{endTime}}\n\n请提前准备好相关物品，准时到达。\n\n乐器室管理团队");

        registerTemplate(NotificationType.BOOKING_REMINDER, NotificationChannel.SITE_MESSAGE,
                "预约即将开始",
                "温馨提醒：您的预约即将开始！练习室：{{roomName}}，时间：{{bookingDate}} {{startTime}}-{{endTime}}。请准时到达。");

        registerTemplate(NotificationType.CHECK_IN_OVERDUE, NotificationChannel.EMAIL,
                "【乐器室】入住逾期提醒",
                "尊敬的{{username}}，您好：\n\n您的预约已超过开始时间30分钟，尚未办理签到。\n\n预约详情：\n- 预约编号：{{bookingId}}\n- 练习室：{{roomName}}\n- 日期：{{bookingDate}}\n- 时间：{{startTime}} - {{endTime}}\n\n如您仍需使用，请尽快办理签到，否则预约将被自动取消。\n\n乐器室管理团队");

        registerTemplate(NotificationType.CHECK_IN_OVERDUE, NotificationChannel.SITE_MESSAGE,
                "入住逾期提醒",
                "您的预约已超过开始时间30分钟尚未签到，请尽快办理，否则预约将被自动取消。");

        registerTemplate(NotificationType.BOOKING_CONFIRMED, NotificationChannel.EMAIL,
                "【乐器室】预约已确认",
                "尊敬的{{username}}，您好：\n\n您的预约已确认！\n\n预约详情：\n- 预约编号：{{bookingId}}\n- 练习室：{{roomName}}\n- 日期：{{bookingDate}}\n- 时间：{{startTime}} - {{endTime}}\n\n期待您的光临！\n\n乐器室管理团队");

        registerTemplate(NotificationType.BOOKING_CONFIRMED, NotificationChannel.SITE_MESSAGE,
                "预约已确认",
                "您的预约已确认！练习室：{{roomName}}，时间：{{bookingDate}} {{startTime}}-{{endTime}}。");

        registerTemplate(NotificationType.BOOKING_COMPLETED, NotificationChannel.EMAIL,
                "【乐器室】预约已完成",
                "尊敬的{{username}}，您好：\n\n您的预约已完成，感谢您的使用！\n\n预约详情：\n- 预约编号：{{bookingId}}\n- 练习室：{{roomName}}\n- 日期：{{bookingDate}}\n- 时间：{{startTime}} - {{endTime}}\n\n期待您的再次光临！\n\n乐器室管理团队");

        registerTemplate(NotificationType.BOOKING_COMPLETED, NotificationChannel.SITE_MESSAGE,
                "预约已完成",
                "您的预约已完成，感谢您的使用！练习室：{{roomName}}，时间：{{bookingDate}} {{startTime}}-{{endTime}}。");

        logger.info("通知模板初始化完成，共注册 {} 个模板", templateCache.size());
    }

    private void registerTemplate(NotificationType type, NotificationChannel channel,
                                  String subjectTemplate, String contentTemplate) {
        NotificationTemplate template = NotificationTemplate.builder()
                .type(type)
                .channel(channel)
                .subjectTemplate(subjectTemplate)
                .contentTemplate(contentTemplate)
                .build();
        String key = getTemplateKey(type, channel);
        templateCache.put(key, template);
    }

    private String getTemplateKey(NotificationType type, NotificationChannel channel) {
        return type.name() + "_" + channel.name();
    }

    public NotificationTemplate getTemplate(NotificationType type, NotificationChannel channel) {
        String key = getTemplateKey(type, channel);
        NotificationTemplate template = templateCache.get(key);
        if (template == null) {
            logger.warn("未找到模板：类型={}, 渠道={}", type, channel);
        }
        return template;
    }

    public RenderedTemplate render(NotificationType type, NotificationChannel channel,
                                   Map<String, Object> variables) {
        NotificationTemplate template = getTemplate(type, channel);
        if (template == null) {
            return null;
        }

        String subject = renderString(template.getSubjectTemplate(), variables);
        String content = renderString(template.getContentTemplate(), variables);

        return RenderedTemplate.builder()
                .subject(subject)
                .content(content)
                .build();
    }

    private String renderString(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
