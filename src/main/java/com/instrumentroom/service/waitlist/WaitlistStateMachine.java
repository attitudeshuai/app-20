package com.instrumentroom.service.waitlist;

import com.instrumentroom.entity.Waitlist;
import com.instrumentroom.entity.WaitlistStatus;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.repository.WaitlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class WaitlistStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistStateMachine.class);

    private final WaitlistRepository waitlistRepository;

    private static final Map<WaitlistStatus, Set<WaitlistStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(WaitlistStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(WaitlistStatus.WAITING, EnumSet.of(
                WaitlistStatus.PROCESSING,
                WaitlistStatus.CANCELLED,
                WaitlistStatus.EXPIRED
        ));
        ALLOWED_TRANSITIONS.put(WaitlistStatus.PROCESSING, EnumSet.of(
                WaitlistStatus.CONFIRMED,
                WaitlistStatus.FAILED,
                WaitlistStatus.WAITING,
                WaitlistStatus.CANCELLED
        ));
        ALLOWED_TRANSITIONS.put(WaitlistStatus.FAILED, EnumSet.of(
                WaitlistStatus.WAITING,
                WaitlistStatus.CANCELLED,
                WaitlistStatus.EXPIRED
        ));
        ALLOWED_TRANSITIONS.put(WaitlistStatus.CONFIRMED, EnumSet.noneOf(WaitlistStatus.class));
        ALLOWED_TRANSITIONS.put(WaitlistStatus.CANCELLED, EnumSet.noneOf(WaitlistStatus.class));
        ALLOWED_TRANSITIONS.put(WaitlistStatus.EXPIRED, EnumSet.noneOf(WaitlistStatus.class));
    }

    public WaitlistStateMachine(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    public boolean canTransition(WaitlistStatus currentStatus, WaitlistStatus targetStatus) {
        Set<WaitlistStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowed != null && allowed.contains(targetStatus);
    }

    @Transactional
    public Waitlist transition(Long waitlistId, WaitlistStatus targetStatus) {
        return transition(waitlistId, targetStatus, null, null);
    }

    @Transactional
    public Waitlist transition(Long waitlistId, WaitlistStatus targetStatus, String reason) {
        return transition(waitlistId, targetStatus, reason, null);
    }

    @Transactional
    public Waitlist transition(Long waitlistId, WaitlistStatus targetStatus, String reason, Long confirmedBookingId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new BusinessException("候补记录不存在"));
        return transition(waitlist, targetStatus, reason, confirmedBookingId);
    }

    @Transactional
    public Waitlist transition(Waitlist waitlist, WaitlistStatus targetStatus) {
        return transition(waitlist, targetStatus, null, null);
    }

    @Transactional
    public Waitlist transition(Waitlist waitlist, WaitlistStatus targetStatus, String reason) {
        return transition(waitlist, targetStatus, reason, null);
    }

    @Transactional
    public Waitlist transition(Waitlist waitlist, WaitlistStatus targetStatus, String reason, Long confirmedBookingId) {
        WaitlistStatus currentStatus = waitlist.getStatus();
        Integer currentVersion = waitlist.getVersion();

        if (!canTransition(currentStatus, targetStatus)) {
            throw new BusinessException(String.format(
                    "无法从状态 %s 转换到 %s", currentStatus, targetStatus));
        }

        int updatedRows;
        if (targetStatus == WaitlistStatus.CONFIRMED && confirmedBookingId != null) {
            updatedRows = waitlistRepository.confirmStatusWithBookingId(
                    waitlist.getId(), targetStatus, confirmedBookingId, currentStatus, currentVersion);
            waitlist.setConfirmedBookingId(confirmedBookingId);
        } else if (targetStatus == WaitlistStatus.FAILED && reason != null) {
            updatedRows = waitlistRepository.updateStatusAndFailReason(
                    waitlist.getId(), targetStatus, reason, currentStatus, currentVersion);
            waitlist.setFailReason(reason);
        } else {
            updatedRows = waitlistRepository.updateStatusWithVersion(
                    waitlist.getId(), targetStatus, currentStatus, currentVersion);
        }

        if (updatedRows == 0) {
            throw new BusinessException("状态变更失败，可能存在并发冲突，请重试");
        }

        waitlist.setStatus(targetStatus);
        waitlist.setVersion(currentVersion + 1);

        logger.info("候补记录状态变更: id={}, from={}, to={}, version={}->{}, reason={}, bookingId={}",
                waitlist.getId(), currentStatus, targetStatus, currentVersion, waitlist.getVersion(),
                reason, confirmedBookingId);

        return waitlist;
    }

    @Transactional
    public boolean tryTransition(Waitlist waitlist, WaitlistStatus targetStatus) {
        return tryTransition(waitlist, targetStatus, null);
    }

    @Transactional
    public boolean tryTransition(Waitlist waitlist, WaitlistStatus targetStatus, String reason) {
        try {
            transition(waitlist, targetStatus, reason);
            return true;
        } catch (BusinessException e) {
            logger.debug("候补状态转换失败: id={}, from={}, to={}, error={}",
                    waitlist.getId(), waitlist.getStatus(), targetStatus, e.getMessage());
            return false;
        }
    }

    public void validateTransition(WaitlistStatus currentStatus, WaitlistStatus targetStatus) {
        if (!canTransition(currentStatus, targetStatus)) {
            throw new BusinessException(String.format(
                    "状态转换不合法: %s -> %s", currentStatus, targetStatus));
        }
    }
}
