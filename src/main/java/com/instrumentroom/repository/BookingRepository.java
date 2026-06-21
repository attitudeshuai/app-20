package com.instrumentroom.repository;

import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约数据访问层
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByRoomId(Long roomId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE " +
           "b.room.id = :roomId AND " +
           "b.bookingDate = :bookingDate AND " +
           "b.status NOT IN ('CANCELLED') AND " +
           "((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT b FROM Booking b WHERE " +
           "(:userId IS NULL OR b.user.id = :userId) AND " +
           "(:roomId IS NULL OR b.room.id = :roomId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:bookingDate IS NULL OR b.bookingDate = :bookingDate)")
    Page<Booking> searchBookings(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("status") BookingStatus status,
            @Param("bookingDate") LocalDate bookingDate,
            Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByStatus(@Param("status") BookingStatus status);

    @Query("SELECT b.bookingDate, COUNT(b) FROM Booking b " +
           "WHERE b.bookingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY b.bookingDate ORDER BY b.bookingDate")
    List<Object[]> getBookingTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT b.room.name, COUNT(b) FROM Booking b " +
           "GROUP BY b.room.id ORDER BY COUNT(b) DESC")
    List<Object[]> getRoomRanking(Pageable pageable);
}
