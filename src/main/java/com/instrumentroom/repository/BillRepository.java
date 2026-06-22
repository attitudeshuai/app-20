package com.instrumentroom.repository;

import com.instrumentroom.entity.Bill;
import com.instrumentroom.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBookingId(Long bookingId);

    Page<Bill> findByUserId(Long userId, Pageable pageable);

    Page<Bill> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    Page<Bill> findByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE " +
           "(:userId IS NULL OR b.user.id = :userId) AND " +
           "(:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus)")
    Page<Bill> searchBills(
            @Param("userId") Long userId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable);

    boolean existsByBookingId(Long bookingId);
}
