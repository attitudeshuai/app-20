package com.instrumentroom.repository;

import com.instrumentroom.entity.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 签到数据访问层
 */
@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByBookingId(Long bookingId);

    @Query("SELECT c FROM CheckIn c WHERE " +
           "(:bookingId IS NULL OR c.booking.id = :bookingId) AND " +
           "(:roomId IS NULL OR c.booking.room.id = :roomId) AND " +
           "(:userId IS NULL OR c.booking.user.id = :userId)")
    Page<CheckIn> searchCheckIns(
            @Param("bookingId") Long bookingId,
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            Pageable pageable);

    boolean existsByBookingId(Long bookingId);
}
