package com.instrumentroom.repository;

import com.instrumentroom.entity.Waitlist;
import com.instrumentroom.entity.WaitlistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    @Query("SELECT w FROM Waitlist w JOIN FETCH w.room JOIN FETCH w.user " +
           "WHERE w.room.id = :roomId " +
           "AND w.bookingDate = :bookingDate " +
           "AND w.status = :status " +
           "AND (w.startTime < :endTime AND w.endTime > :startTime) " +
           "ORDER BY w.priority DESC, w.createdAt ASC")
    List<Waitlist> findEligibleWaitlistsWithDetails(
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("status") WaitlistStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Waitlist w " +
           "WHERE w.room.id = :roomId " +
           "AND w.bookingDate = :bookingDate " +
           "AND w.status = :status " +
           "AND (w.startTime < :endTime AND w.endTime > :startTime) " +
           "ORDER BY w.priority DESC, w.createdAt ASC")
    List<Waitlist> findEligibleWaitlistsWithLock(
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("status") WaitlistStatus status);

    @Query("SELECT COUNT(w) FROM Waitlist w " +
           "WHERE w.room.id = :roomId " +
           "AND w.bookingDate = :bookingDate " +
           "AND w.status = :status " +
           "AND ((w.startTime < :endTime AND w.endTime > :startTime))")
    long countEligibleWaitlists(
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("status") WaitlistStatus status);

    @Query("SELECT w FROM Waitlist w JOIN FETCH w.room JOIN FETCH w.user WHERE w.id = :id")
    Optional<Waitlist> findByIdWithDetails(@Param("id") Long id);

    Page<Waitlist> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT w FROM Waitlist w WHERE " +
           "(:userId IS NULL OR w.user.id = :userId) AND " +
           "(:roomId IS NULL OR w.room.id = :roomId) AND " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:bookingDate IS NULL OR w.bookingDate = :bookingDate)")
    Page<Waitlist> searchWaitlists(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("status") WaitlistStatus status,
            @Param("bookingDate") LocalDate bookingDate,
            Pageable pageable);

    @Query("SELECT COUNT(w) FROM Waitlist w " +
           "WHERE w.user.id = :userId " +
           "AND w.room.id = :roomId " +
           "AND w.bookingDate = :bookingDate " +
           "AND w.status IN :statuses " +
           "AND (w.startTime < :endTime AND w.endTime > :startTime)")
    long countUserWaitlistInTimeRange(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<WaitlistStatus> statuses);

    @Query("SELECT w FROM Waitlist w WHERE w.status IN :statuses AND w.expireAt <= :now")
    List<Waitlist> findExpiredWaitlists(@Param("statuses") List<WaitlistStatus> statuses, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Waitlist w SET w.status = :newStatus, w.version = w.version + 1 WHERE w.id = :id AND w.status = :expectedStatus AND w.version = :version")
    int updateStatusWithVersion(
            @Param("id") Long id,
            @Param("newStatus") WaitlistStatus newStatus,
            @Param("expectedStatus") WaitlistStatus expectedStatus,
            @Param("version") Integer version);

    @Modifying
    @Query("UPDATE Waitlist w SET w.status = :newStatus, w.confirmedBookingId = :confirmedBookingId, w.version = w.version + 1 WHERE w.id = :id AND w.status = :expectedStatus AND w.version = :version")
    int confirmStatusWithBookingId(
            @Param("id") Long id,
            @Param("newStatus") WaitlistStatus newStatus,
            @Param("confirmedBookingId") Long confirmedBookingId,
            @Param("expectedStatus") WaitlistStatus expectedStatus,
            @Param("version") Integer version);

    @Modifying
    @Query("UPDATE Waitlist w SET w.status = :newStatus, w.failReason = :failReason, w.version = w.version + 1 WHERE w.id = :id")
    int updateStatusAndFailReason(@Param("id") Long id, @Param("newStatus") WaitlistStatus newStatus, @Param("failReason") String failReason);

    @Query("SELECT w FROM Waitlist w WHERE w.status = :status AND w.bookingDate <= :date ORDER BY w.createdAt ASC")
    List<Waitlist> findByStatusAndBookingDateBefore(
            @Param("status") WaitlistStatus status,
            @Param("date") LocalDate date);
}
