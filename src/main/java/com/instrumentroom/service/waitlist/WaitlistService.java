package com.instrumentroom.service.waitlist;

import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.dto.waitlist.CreateWaitlistRequest;
import com.instrumentroom.dto.waitlist.UpdateWaitlistPriorityRequest;
import com.instrumentroom.dto.waitlist.WaitlistResponse;
import com.instrumentroom.entity.*;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.repository.WaitlistRepository;
import com.instrumentroom.service.AuthService;
import com.instrumentroom.service.PracticeRoomService;
import com.instrumentroom.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class WaitlistService {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistService.class);

    private final WaitlistRepository waitlistRepository;
    private final WaitlistRuleService ruleService;
    private final WaitlistStateMachine stateMachine;
    private final WaitlistPromotionService promotionService;
    private final PracticeRoomService roomService;
    private final AuthService authService;
    private final NotificationService notificationService;

    public WaitlistService(WaitlistRepository waitlistRepository,
                           WaitlistRuleService ruleService,
                           WaitlistStateMachine stateMachine,
                           WaitlistPromotionService promotionService,
                           PracticeRoomService roomService,
                           AuthService authService,
                           NotificationService notificationService) {
        this.waitlistRepository = waitlistRepository;
        this.ruleService = ruleService;
        this.stateMachine = stateMachine;
        this.promotionService = promotionService;
        this.roomService = roomService;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    public WaitlistResponse createWaitlist(CreateWaitlistRequest request) {
        ruleService.validateWaitlistSubmission(request);

        User currentUser = authService.getCurrentUserEntity();
        PracticeRoom room = roomService.getRoomEntityById(request.getRoomId());

        int priority = ruleService.calculatePriority(request.getPriority());
        LocalDateTime expireAt = ruleService.calculateExpireTime(
                request.getBookingDate(), request.getStartTime());

        Waitlist waitlist = Waitlist.builder()
                .room(room)
                .user(currentUser)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .purpose(request.getPurpose())
                .status(WaitlistStatus.WAITING)
                .priority(priority)
                .expireAt(expireAt)
                .build();

        waitlist = waitlistRepository.save(waitlist);

        int queuePosition = ruleService.calculateQueuePosition(
                room.getId(), request.getBookingDate(),
                request.getStartTime(), request.getEndTime(),
                priority, waitlist.getCreatedAt());
        waitlist.setQueuePosition(queuePosition);
        waitlist = waitlistRepository.save(waitlist);

        notificationService.notifyWaitlistCreated(waitlist);

        logger.info("候补申请创建成功: id={}, userId={}, roomId={}, date={}, time={}-{}",
                waitlist.getId(), currentUser.getId(), room.getId(),
                request.getBookingDate(), request.getStartTime(), request.getEndTime());

        return WaitlistResponse.fromEntity(waitlist, true);
    }

    public PageResponse<WaitlistResponse> getWaitlists(
            Long userId, Long roomId, WaitlistStatus status,
            LocalDate bookingDate, int page, int size,
            String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Waitlist> waitlistPage = waitlistRepository.searchWaitlists(
                userId, roomId, status, bookingDate, pageable);

        return PageResponse.from(waitlistPage, w -> WaitlistResponse.fromEntity(w, false));
    }

    public PageResponse<WaitlistResponse> getMyWaitlists(
            WaitlistStatus status, int page, int size) {

        User currentUser = authService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Waitlist> waitlistPage;
        if (status != null) {
            waitlistPage = waitlistRepository.searchWaitlists(
                    currentUser.getId(), null, status, null, pageable);
        } else {
            waitlistPage = waitlistRepository.findByUserId(currentUser.getId(), pageable);
        }

        return PageResponse.from(waitlistPage, w -> WaitlistResponse.fromEntity(w, true));
    }

    public WaitlistResponse getWaitlistById(Long id) {
        Waitlist waitlist = getWaitlistEntityById(id);
        checkViewPermission(waitlist);
        return WaitlistResponse.fromEntity(waitlist, true);
    }

    public Waitlist getWaitlistEntityById(Long id) {
        return waitlistRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("候补记录", "id", id));
    }

    @Transactional
    public WaitlistResponse cancelWaitlist(Long id) {
        Waitlist waitlist = getWaitlistEntityById(id);
        checkModifyPermission(waitlist);

        stateMachine.transition(waitlist, WaitlistStatus.CANCELLED);

        notificationService.notifyWaitlistCancelled(waitlist, "用户取消");

        logger.info("候补记录已取消: id={}, userId={}", id, waitlist.getUser().getId());

        return WaitlistResponse.fromEntity(waitlist, true);
    }

    @Transactional
    public WaitlistResponse updatePriority(Long id, UpdateWaitlistPriorityRequest request) {
        Waitlist waitlist = getWaitlistEntityById(id);
        checkModifyPermission(waitlist);

        ruleService.validatePriorityChange(waitlist, request.getPriority());

        int oldPriority = waitlist.getPriority();
        waitlist.setPriority(request.getPriority());

        int queuePosition = ruleService.calculateQueuePosition(
                waitlist.getRoom().getId(), waitlist.getBookingDate(),
                waitlist.getStartTime(), waitlist.getEndTime(),
                request.getPriority(), waitlist.getCreatedAt());
        waitlist.setQueuePosition(queuePosition);
        waitlist = waitlistRepository.save(waitlist);

        logger.info("候补优先级更新: id={}, oldPriority={}, newPriority={}, position={}",
                id, oldPriority, request.getPriority(), queuePosition);

        return WaitlistResponse.fromEntity(waitlist, true);
    }

    @Transactional
    public int triggerPromotion(Long roomId, LocalDate bookingDate,
                                LocalTime startTime, LocalTime endTime) {
        if (!authService.isAdmin()) {
            throw new BusinessException(403, "无权触发转正");
        }
        return promotionService.promoteWaitlistsForSlot(roomId, bookingDate, startTime, endTime);
    }

    @Transactional
    public void cleanExpiredWaitlists() {
        List<WaitlistStatus> activeStatuses = List.of(WaitlistStatus.WAITING, WaitlistStatus.PROCESSING);
        List<Waitlist> expiredWaitlists = waitlistRepository.findExpiredWaitlists(
                activeStatuses, LocalDateTime.now());

        int count = 0;
        for (Waitlist waitlist : expiredWaitlists) {
            try {
                stateMachine.transition(waitlist, WaitlistStatus.EXPIRED);
                notificationService.notifyWaitlistExpired(waitlist);
                count++;
            } catch (Exception e) {
                logger.error("处理过期候补失败: id={}", waitlist.getId(), e);
            }
        }

        if (count > 0) {
            logger.info("清理过期候补完成，共处理 {} 条", count);
        }
    }

    private void checkViewPermission(Waitlist waitlist) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(waitlist.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权查看此候补记录");
        }
    }

    private void checkModifyPermission(Waitlist waitlist) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(waitlist.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权修改此候补记录");
        }
    }
}
