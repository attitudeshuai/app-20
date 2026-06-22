package com.instrumentroom.service;

import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.CheckIn;
import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.repository.CheckInRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class BillingCalculationService {

    private final CheckInRepository checkInRepository;

    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal(60);
    private static final int MINIMUM_BILLING_MINUTES = 15;
    private static final int BILLING_GRANULARITY_MINUTES = 15;

    public BillingCalculationService(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    public int calculateBookedDurationMinutes(Booking booking) {
        LocalTime startTime = booking.getStartTime();
        LocalTime endTime = booking.getEndTime();
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    public Integer calculateActualDurationMinutes(Booking booking) {
        Optional<CheckIn> checkInOpt = checkInRepository.findByBookingId(booking.getId());
        if (checkInOpt.isEmpty()) {
            return null;
        }
        CheckIn checkIn = checkInOpt.get();
        if (checkIn.getCheckInAt() == null || checkIn.getCheckOutAt() == null) {
            return null;
        }
        long minutes = Duration.between(checkIn.getCheckInAt(), checkIn.getCheckOutAt()).toMinutes();
        return applyBillingRules((int) minutes);
    }

    private int applyBillingRules(int actualMinutes) {
        if (actualMinutes <= 0) {
            return 0;
        }
        if (actualMinutes < MINIMUM_BILLING_MINUTES) {
            return MINIMUM_BILLING_MINUTES;
        }
        int remainder = actualMinutes % BILLING_GRANULARITY_MINUTES;
        if (remainder == 0) {
            return actualMinutes;
        }
        return actualMinutes + (BILLING_GRANULARITY_MINUTES - remainder);
    }

    public BigDecimal calculateAmount(BigDecimal hourlyPrice, int durationMinutes) {
        if (durationMinutes <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal durationHours = new BigDecimal(durationMinutes)
                .divide(MINUTES_PER_HOUR, 4, RoundingMode.HALF_UP);
        return hourlyPrice.multiply(durationHours).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateBookedAmount(Booking booking) {
        PracticeRoom room = booking.getRoom();
        int bookedMinutes = calculateBookedDurationMinutes(booking);
        return calculateAmount(room.getHourlyPrice(), bookedMinutes);
    }

    public BigDecimal calculateActualAmount(Booking booking) {
        PracticeRoom room = booking.getRoom();
        Integer actualMinutes = calculateActualDurationMinutes(booking);
        if (actualMinutes == null) {
            return null;
        }
        return calculateAmount(room.getHourlyPrice(), actualMinutes);
    }

    public BigDecimal calculateTotalAmount(BigDecimal baseAmount, BigDecimal discountAmount) {
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal total = baseAmount.subtract(discount);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    public BigDecimal calculateHourlyPrice(Booking booking) {
        return booking.getRoom().getHourlyPrice();
    }
}
