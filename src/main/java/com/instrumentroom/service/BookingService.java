package com.instrumentroom.service;

import com.instrumentroom.dto.booking.*;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.entity.*;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.notification.NotificationService;
import com.instrumentroom.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约管理服务
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PracticeRoomService roomService;
    private final AuthService authService;
    private final NotificationService notificationService;

    public BookingService(
            BookingRepository bookingRepository,
            PracticeRoomService roomService,
            AuthService authService,
            NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    /**
     * 创建预约
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        PracticeRoom room = roomService.getRoomEntityById(request.getRoomId());

        // 检查练习室状态
        if (room.getStatus() != RoomStatus.OPEN) {
            throw new BusinessException("练习室当前不可预约");
        }

        // 验证时间
        LocalDate bookingDate = request.getBookingDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new BusinessException("预约日期不能早于今天");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        if (startTime.isBefore(room.getOpenTime()) || endTime.isAfter(room.getCloseTime())) {
            throw new BusinessException("预约时间超出练习室开放时间范围");
        }

        // 检查时间冲突
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                room.getId(), bookingDate, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时段已被预约，请选择其他时间");
        }

        Booking booking = Booking.builder()
                .room(room)
                .user(currentUser)
                .bookingDate(bookingDate)
                .startTime(startTime)
                .endTime(endTime)
                .purpose(request.getPurpose())
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        notificationService.notifyBookingCreated(booking);

        return BookingResponse.fromEntity(booking, true);
    }

    /**
     * 获取预约列表（支持分页、搜索、筛选）
     */
    public PageResponse<BookingResponse> getBookings(
            Long userId,
            Long roomId,
            BookingStatus status,
            LocalDate bookingDate,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Booking> bookingPage = bookingRepository.searchBookings(userId, roomId, status, bookingDate, pageable);

        return PageResponse.from(bookingPage, b -> BookingResponse.fromEntity(b, false));
    }

    /**
     * 获取我的预约
     */
    public PageResponse<BookingResponse> getMyBookings(
            BookingStatus status,
            int page,
            int size) {

        User currentUser = authService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Booking> bookingPage;
        if (status != null) {
            bookingPage = bookingRepository.searchBookings(currentUser.getId(), null, status, null, pageable);
        } else {
            bookingPage = bookingRepository.findByUserId(currentUser.getId(), pageable);
        }

        return PageResponse.from(bookingPage, b -> BookingResponse.fromEntity(b, true));
    }

    /**
     * 获取预约详情
     */
    public BookingResponse getBookingById(Long id) {
        Booking booking = getBookingEntityById(id);
        checkViewPermission(booking);
        return BookingResponse.fromEntity(booking, true);
    }

    /**
     * 根据ID获取预约实体
     */
    public Booking getBookingEntityById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("预约", "id", id));
    }

    /**
     * 更新预约
     */
    @Transactional
    public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
        Booking booking = getBookingEntityById(id);
        checkModifyPermission(booking);

        if (request.getRoomId() != null) {
            PracticeRoom newRoom = roomService.getRoomEntityById(request.getRoomId());
            if (newRoom.getStatus() != RoomStatus.OPEN) {
                throw new BusinessException("练习室当前不可预约");
            }
            booking.setRoom(newRoom);
        }

        PracticeRoom room = booking.getRoom();
        LocalDate bookingDate = request.getBookingDate() != null ? request.getBookingDate() : booking.getBookingDate();
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : booking.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : booking.getEndTime();

        if (request.getBookingDate() != null) {
            if (bookingDate.isBefore(LocalDate.now())) {
                throw new BusinessException("预约日期不能早于今天");
            }
            booking.setBookingDate(bookingDate);
        }
        if (request.getStartTime() != null || request.getEndTime() != null) {
            if (!startTime.isBefore(endTime)) {
                throw new BusinessException("开始时间必须早于结束时间");
            }
            if (startTime.isBefore(room.getOpenTime()) || endTime.isAfter(room.getCloseTime())) {
                throw new BusinessException("预约时间超出练习室开放时间范围");
            }

            // 检查时间冲突（排除自身）
            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    room.getId(), bookingDate, startTime, endTime);
            final Long currentBookingId = booking.getId();
            boolean hasConflict = conflicts.stream()
                    .anyMatch(b -> !b.getId().equals(currentBookingId));
            if (hasConflict) {
                throw new BusinessException("该时段已被预约，请选择其他时间");
            }

            booking.setStartTime(startTime);
            booking.setEndTime(endTime);
        }

        if (request.getPurpose() != null) {
            booking.setPurpose(request.getPurpose());
        }
        if (request.getStatus() != null) {
            checkStatusChangePermission(request.getStatus());
            booking.setStatus(request.getStatus());
        }

        booking = bookingRepository.save(booking);
        return BookingResponse.fromEntity(booking, true);
    }

    /**
     * 更新预约状态
     */
    @Transactional
    public BookingResponse updateBookingStatus(Long id, UpdateBookingStatusRequest request) {
        Booking booking = getBookingEntityById(id);
        checkModifyPermission(booking);
        checkStatusChangePermission(request.getStatus());

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus = request.getStatus();

        booking.setStatus(newStatus);
        booking = bookingRepository.save(booking);

        handleStatusChangeNotification(booking, oldStatus, newStatus);

        return BookingResponse.fromEntity(booking, true);
    }

    /**
     * 删除预约
     */
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getBookingEntityById(id);
        checkModifyPermission(booking);

        notificationService.notifyBookingCancelled(booking, "用户删除");

        bookingRepository.delete(booking);
    }

    private void handleStatusChangeNotification(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        if (oldStatus == newStatus) {
            return;
        }

        switch (newStatus) {
            case CANCELLED:
                notificationService.notifyBookingCancelled(booking, "状态变更");
                break;
            case CONFIRMED:
                notificationService.notifyBookingConfirmed(booking);
                break;
            case COMPLETED:
                notificationService.notifyBookingCompleted(booking);
                break;
            default:
                break;
        }
    }

    /**
     * 检查查看权限（预约创建者或管理员）
     */
    private void checkViewPermission(Booking booking) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(booking.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权查看此预约");
        }
    }

    /**
     * 检查修改权限（预约创建者或管理员）
     */
    private void checkModifyPermission(Booking booking) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(booking.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权修改此预约");
        }
    }

    /**
     * 检查状态变更权限
     */
    private void checkStatusChangePermission(BookingStatus status) {
        // 某些状态变更可能需要管理员权限，这里可以细化
    }
}
