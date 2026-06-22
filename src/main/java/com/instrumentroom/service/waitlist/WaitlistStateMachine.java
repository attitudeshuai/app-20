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
        return transition(waitlistId, targetStatus, null);
    }

    @Transactional
    public Waitlist transition(Long waitlistId, WaitlistStatus targetStatus, String reason) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new BusinessException("候补记录不存在"));
        return transition(waitlist, targetStatus, reason);
    }

    @Transactional
    public Waitlist transition(Waitlist waitlist, WaitlistStatus targetStatus) {
        return transition(waitlist, targetStatus, null);
    }

    @Transactional
    public Waitlist transition(Waitlist waitlist, WaitlistStatus targetStatus, String reason) {
        WaitlistStatus currentStatus = waitlist.getStatus();

        if (!canTransition(currentStatus, targetStatus)) {
            throw new BusinessException(String.format(
                    "无法从状态 %s 转换到 %s", currentStatus, targetStatus));
        }

        int updatedRows = waitlistRepository.updateStatusWithVersion(
                waitlist.getId(), targetStatus, currentStatus, waitlist.getVersion());

        if (updatedRows == 0) {
            throw new BusinessException("状态变更失败，可能存在并发冲突，请重试");
        }

        waitlist.setStatus(targetStatus);
        if (reason != null && targetStatus == WaitlistStatus.FAILED) {
            waitlist.setFailReason(reason);
        }

        logger.info("候补记录状态变更: id={}, from={}, to={}, reason={}",
                waitlist.getId(), currentStatus, targetStatus, reason);

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
