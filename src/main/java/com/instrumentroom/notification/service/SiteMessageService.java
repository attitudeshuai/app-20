package com.instrumentroom.notification.service;

import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.entity.User;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.notification.dto.SiteMessageResponse;
import com.instrumentroom.notification.entity.SiteMessage;
import com.instrumentroom.notification.repository.SiteMessageRepository;
import com.instrumentroom.service.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SiteMessageService {

    private final SiteMessageRepository siteMessageRepository;
    private final AuthService authService;

    public SiteMessageService(SiteMessageRepository siteMessageRepository,
                              AuthService authService) {
        this.siteMessageRepository = siteMessageRepository;
        this.authService = authService;
    }

    public PageResponse<SiteMessageResponse> getMyMessages(Boolean isRead, int page, int size) {
        User currentUser = authService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page, size);

        Page<SiteMessage> messagePage;
        if (isRead != null) {
            messagePage = siteMessageRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
                    currentUser.getId(), isRead, pageable);
        } else {
            messagePage = siteMessageRepository.findByUserIdOrderByCreatedAtDesc(
                    currentUser.getId(), pageable);
        }

        return PageResponse.from(messagePage, SiteMessageResponse::fromEntity);
    }

    public Map<String, Object> getUnreadCount() {
        User currentUser = authService.getCurrentUserEntity();
        long count = siteMessageRepository.countByUserIdAndIsRead(currentUser.getId(), false);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    @Transactional
    public void markAsRead(Long id) {
        User currentUser = authService.getCurrentUserEntity();
        SiteMessage message = siteMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("消息", "id", id));
        if (!message.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("消息", "id", id);
        }
        siteMessageRepository.markAsRead(id, currentUser.getId(), LocalDateTime.now());
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = authService.getCurrentUserEntity();
        siteMessageRepository.markAllAsRead(currentUser.getId(), LocalDateTime.now());
    }
}
