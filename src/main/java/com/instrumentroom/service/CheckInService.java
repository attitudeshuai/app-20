package com.instrumentroom.service;

import com.instrumentroom.dto.checkin.CheckInResponse;
import com.instrumentroom.dto.checkin.CreateCheckInRequest;
import com.instrumentroom.dto.checkin.UpdateCheckInRequest;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.BookingStatus;
import com.instrumentroom.entity.CheckIn;
import com.instrumentroom.entity.User;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.notification.NotificationService;
import com.instrumentroom.repository.CheckInRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 签到管理服务
 */
@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final BookingService bookingService;
    private final AuthService authService;
    private final NotificationService notificationService;

    public CheckInService(
            CheckInRepository checkInRepository,
            BookingService bookingService,
            AuthService authService,
            NotificationService notificationService) {
        this.checkInRepository = checkInRepository;
        this.bookingService = bookingService;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    /**
     * 创建签到
     */
    @Transactional
    public CheckInResponse createCheckIn(CreateCheckInRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        Booking booking = bookingService.getBookingEntityById(request.getBookingId());

        // 检查权限
        if (!currentUser.getId().equals(booking.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权为此预约签到");
        }

        // 检查预约状态
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("只有待确认或已确认的预约才能签到");
        }

        // 检查是否已签到
        if (checkInRepository.existsByBookingId(request.getBookingId())) {
            throw new BusinessException("该预约已签到");
        }

        CheckIn checkIn = CheckIn.builder()
                .booking(booking)
                .checkInAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        checkIn = checkInRepository.save(checkIn);

        // 自动将预约状态更新为已完成（如果尚未更新）
        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
            // 不自动更改，留给签出处理或管理员处理
        }

        return CheckInResponse.fromEntity(checkIn, true);
    }

    /**
     * 获取签到列表（支持分页、搜索、筛选）
     */
    public PageResponse<CheckInResponse> getCheckIns(
            Long bookingId,
            Long roomId,
            Long userId,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CheckIn> checkInPage = checkInRepository.searchCheckIns(bookingId, roomId, userId, pageable);

        return PageResponse.from(checkInPage, c -> CheckInResponse.fromEntity(c, false));
    }

    /**
     * 获取签到详情
     */
    public CheckInResponse getCheckInById(Long id) {
        CheckIn checkIn = getCheckInEntityById(id);
        checkViewPermission(checkIn);
        return CheckInResponse.fromEntity(checkIn, true);
    }

    /**
     * 根据ID获取签到实体
     */
    public CheckIn getCheckInEntityById(Long id) {
        return checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("签到记录", "id", id));
    }

    /**
     * 更新签到（签出）
     */
    @Transactional
    public CheckInResponse updateCheckIn(Long id, UpdateCheckInRequest request) {
        CheckIn checkIn = getCheckInEntityById(id);
        checkModifyPermission(checkIn);

        if (Boolean.TRUE.equals(request.getCheckOut())) {
            if (checkIn.getCheckOutAt() != null) {
                throw new BusinessException("已签出，不能重复签出");
            }
            checkIn.setCheckOutAt(LocalDateTime.now());

            // 自动将预约状态更新为已完成
            Booking booking = checkIn.getBooking();
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.COMPLETED);
                notificationService.notifyBookingCompleted(booking);
            }
        }

        if (request.getNote() != null) {
            checkIn.setNote(request.getNote());
        }

        checkIn = checkInRepository.save(checkIn);
        return CheckInResponse.fromEntity(checkIn, true);
    }

    /**
     * 删除签到记录
     */
    @Transactional
    public void deleteCheckIn(Long id) {
        CheckIn checkIn = getCheckInEntityById(id);
        checkModifyPermission(checkIn);
        checkInRepository.delete(checkIn);
    }

    /**
     * 检查查看权限
     */
    private void checkViewPermission(CheckIn checkIn) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(checkIn.getBooking().getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权查看此签到记录");
        }
    }

    /**
     * 检查修改权限
     */
    private void checkModifyPermission(CheckIn checkIn) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(checkIn.getBooking().getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权修改此签到记录");
        }
    }
}
