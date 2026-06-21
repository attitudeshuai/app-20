package com.instrumentroom.config;

import com.instrumentroom.entity.*;
import com.instrumentroom.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 数据初始化类 - 容器启动后自动插入示例数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PracticeRoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;
    private final RoomIssueRepository issueRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            PracticeRoomRepository roomRepository,
            BookingRepository bookingRepository,
            CheckInRepository checkInRepository,
            RoomIssueRepository issueRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.checkInRepository = checkInRepository;
        this.issueRepository = issueRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("数据已存在，跳过初始化");
            return;
        }

        logger.info("开始初始化示例数据...");

        // 1. 创建用户
        User admin = createUser("admin", "admin@instrumentroom.com", "admin123", "ADMIN");
        User user1 = createUser("zhangsan", "zhangsan@example.com", "password123", "USER");
        User user2 = createUser("lisi", "lisi@example.com", "password123", "USER");
        User user3 = createUser("wangwu", "wangwu@example.com", "password123", "USER");
        User user4 = createUser("zhaoliu", "zhaoliu@example.com", "password123", "USER");

        // 2. 创建练习室
        PracticeRoom room1 = createRoom("钢琴练习室A", "一楼101室", 2,
                "雅马哈三角钢琴 x1, 琴凳 x2, 乐谱架 x2",
                new BigDecimal("50.00"), LocalTime.of(8, 0), LocalTime.of(22, 0), RoomStatus.OPEN);
        PracticeRoom room2 = createRoom("钢琴练习室B", "一楼102室", 2,
                "卡哇伊立式钢琴 x1, 琴凳 x2",
                new BigDecimal("40.00"), LocalTime.of(8, 0), LocalTime.of(22, 0), RoomStatus.OPEN);
        PracticeRoom room3 = createRoom("小提琴练习室", "二楼201室", 4,
                "乐谱架 x4, 座椅 x4, 镜子 x1",
                new BigDecimal("30.00"), LocalTime.of(8, 0), LocalTime.of(21, 0), RoomStatus.OPEN);
        PracticeRoom room4 = createRoom("吉他练习室", "二楼202室", 6,
                "乐谱架 x6, 座椅 x6, 音箱 x1",
                new BigDecimal("25.00"), LocalTime.of(9, 0), LocalTime.of(22, 0), RoomStatus.OPEN);
        PracticeRoom room5 = createRoom("架子鼓练习室", "负一楼B01室", 3,
                "架子鼓套装 x1, 隔音垫 x1, 鼓棒 x2",
                new BigDecimal("60.00"), LocalTime.of(9, 0), LocalTime.of(21, 0), RoomStatus.OPEN);
        PracticeRoom room6 = createRoom("多功能合奏室", "三楼301室", 15,
                "三角钢琴 x1, 乐谱架 x15, 座椅 x15, 指挥台 x1",
                new BigDecimal("100.00"), LocalTime.of(8, 0), LocalTime.of(22, 0), RoomStatus.MAINTENANCE);
        PracticeRoom room7 = createRoom("声乐练习室", "二楼203室", 2,
                "钢琴 x1, 麦克风 x1, 音响 x1",
                new BigDecimal("45.00"), LocalTime.of(8, 0), LocalTime.of(21, 0), RoomStatus.OPEN);
        PracticeRoom room8 = createRoom("古筝练习室", "二楼204室", 4,
                "古筝 x4, 琴凳 x4, 乐谱架 x4",
                new BigDecimal("35.00"), LocalTime.of(8, 0), LocalTime.of(21, 0), RoomStatus.CLOSED);

        // 3. 创建预约
        LocalDate today = LocalDate.now();
        Booking booking1 = createBooking(room1, user1, today,
                LocalTime.of(9, 0), LocalTime.of(11, 0), "钢琴考级练习", BookingStatus.CONFIRMED);
        Booking booking2 = createBooking(room3, user2, today,
                LocalTime.of(14, 0), LocalTime.of(16, 0), "小提琴小组练习", BookingStatus.PENDING);
        Booking booking3 = createBooking(room5, user3, today.plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0), "架子鼓独奏排练", BookingStatus.CONFIRMED);
        Booking booking4 = createBooking(room4, user4, today,
                LocalTime.of(19, 0), LocalTime.of(21, 0), "乐队排练", BookingStatus.COMPLETED);
        Booking booking5 = createBooking(room2, user1, today.plusDays(2),
                LocalTime.of(15, 0), LocalTime.of(17, 0), "日常练习", BookingStatus.PENDING);
        Booking booking6 = createBooking(room7, user2, today.plusDays(1),
                LocalTime.of(13, 0), LocalTime.of(14, 30), "声乐课程", BookingStatus.CONFIRMED);

        // 4. 创建签到记录
        createCheckIn(booking4, LocalDateTime(today, LocalTime.of(19, 5)),
                LocalDateTime(today, LocalTime.of(21, 0)), "练习顺利完成");
        createCheckIn(booking1, LocalDateTime(today, LocalTime.of(9, 10)),
                null, "正在练习中...");

        // 5. 创建问题反馈
        createIssue(room1, user2, "噪音问题",
                "隔壁装修噪音很大，影响练习，希望能加强隔音措施", IssueStatus.OPEN);
        createIssue(room5, user3, "设备问题",
                "军鼓的鼓皮有破损，建议更换", IssueStatus.IN_PROGRESS);
        createIssue(room2, user4, "卫生问题",
                "琴凳上有灰尘，请及时清洁", IssueStatus.RESOLVED);
        createIssue(room3, user1, "设备问题",
                "A座位的椅子摇晃不稳", IssueStatus.OPEN);
        createIssue(room6, admin, "维护通知",
                "本周进行空调系统维护，预计3天后开放", IssueStatus.CLOSED);

        logger.info("示例数据初始化完成！");
        logger.info("管理员账号: admin / admin123");
        logger.info("普通用户账号: zhangsan / password123, lisi / password123, wangwu / password123, zhaoliu / password123");
    }

    private User createUser(String username, String email, String password, String role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username)
                .build();
        return userRepository.save(user);
    }

    private PracticeRoom createRoom(String name, String location, int capacity,
                                     String equipment, BigDecimal price,
                                     LocalTime openTime, LocalTime closeTime, RoomStatus status) {
        PracticeRoom room = PracticeRoom.builder()
                .name(name)
                .location(location)
                .capacity(capacity)
                .equipment(equipment)
                .hourlyPrice(price)
                .openTime(openTime)
                .closeTime(closeTime)
                .status(status)
                .build();
        return roomRepository.save(room);
    }

    private Booking createBooking(PracticeRoom room, User user, LocalDate date,
                                   LocalTime start, LocalTime end, String purpose, BookingStatus status) {
        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .bookingDate(date)
                .startTime(start)
                .endTime(end)
                .purpose(purpose)
                .status(status)
                .build();
        return bookingRepository.save(booking);
    }

    private void createCheckIn(Booking booking, java.time.LocalDateTime checkInAt,
                                java.time.LocalDateTime checkOutAt, String note) {
        CheckIn checkIn = CheckIn.builder()
                .booking(booking)
                .checkInAt(checkInAt)
                .checkOutAt(checkOutAt)
                .note(note)
                .build();
        checkInRepository.save(checkIn);
    }

    private void createIssue(PracticeRoom room, User reporter, String type,
                              String description, IssueStatus status) {
        RoomIssue issue = RoomIssue.builder()
                .room(room)
                .reporter(reporter)
                .issueType(type)
                .description(description)
                .status(status)
                .build();
        issueRepository.save(issue);
    }

    private java.time.LocalDateTime LocalDateTime(LocalDate date, LocalTime time) {
        return java.time.LocalDateTime.of(date, time);
    }
}
