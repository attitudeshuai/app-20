package com.instrumentroom.notification.repository;

import com.instrumentroom.notification.NotificationType;
import com.instrumentroom.notification.entity.SiteMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SiteMessageRepository extends JpaRepository<SiteMessage, Long> {

    Page<SiteMessage> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<SiteMessage> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    @Modifying
    @Query("UPDATE SiteMessage m SET m.isRead = true, m.readAt = :readAt WHERE m.id = :id AND m.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE SiteMessage m SET m.isRead = true, m.readAt = :readAt WHERE m.user.id = :userId AND m.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    boolean existsByUserIdAndRelatedIdAndMessageType(Long userId, Long relatedId, NotificationType messageType);
}
