package com.instrumentroom.service;

import com.instrumentroom.dto.billing.BillResponse;
import com.instrumentroom.dto.billing.CreateBillRequest;
import com.instrumentroom.dto.billing.UpdatePaymentStatusRequest;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.entity.*;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.payment.*;
import com.instrumentroom.repository.BillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final BookingService bookingService;
    private final AuthService authService;
    private final BillingCalculationService calculationService;
    private final PaymentGatewayFactory paymentGatewayFactory;

    public BillingService(
            BillRepository billRepository,
            BookingService bookingService,
            AuthService authService,
            BillingCalculationService calculationService,
            PaymentGatewayFactory paymentGatewayFactory) {
        this.billRepository = billRepository;
        this.bookingService = bookingService;
        this.authService = authService;
        this.calculationService = calculationService;
        this.paymentGatewayFactory = paymentGatewayFactory;
    }

    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        Booking booking = bookingService.getBookingEntityById(request.getBookingId());

        validateBookingForBilling(booking);

        if (billRepository.existsByBookingId(booking.getId())) {
            throw new BusinessException("该预约已生成账单");
        }

        checkModifyPermission(booking);

        BigDecimal hourlyPrice = calculationService.calculateHourlyPrice(booking);
        int bookedDurationMinutes = calculationService.calculateBookedDurationMinutes(booking);
        BigDecimal bookedAmount = calculationService.calculateBookedAmount(booking);
        Integer actualDurationMinutes = calculationService.calculateActualDurationMinutes(booking);
        BigDecimal actualAmount = calculationService.calculateActualAmount(booking);

        BigDecimal baseAmount = actualAmount != null ? actualAmount : bookedAmount;
        BigDecimal totalAmount = calculationService.calculateTotalAmount(baseAmount, request.getDiscountAmount());

        Bill bill = Bill.builder()
                .booking(booking)
                .user(booking.getUser())
                .hourlyPrice(hourlyPrice)
                .bookedDurationMinutes(bookedDurationMinutes)
                .actualDurationMinutes(actualDurationMinutes)
                .bookedAmount(bookedAmount)
                .actualAmount(actualAmount)
                .totalAmount(totalAmount)
                .discountAmount(request.getDiscountAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .remark(request.getRemark())
                .build();

        bill = billRepository.save(bill);
        return BillResponse.fromEntity(bill, true);
    }

    public BillResponse getBillById(Long id) {
        Bill bill = getBillEntityById(id);
        checkViewPermission(bill);
        return BillResponse.fromEntity(bill, true);
    }

    public BillResponse getBillByBookingId(Long bookingId) {
        Bill bill = billRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("账单", "bookingId", bookingId));
        checkViewPermission(bill);
        return BillResponse.fromEntity(bill, true);
    }

    public PageResponse<BillResponse> getBills(
            Long userId,
            PaymentStatus paymentStatus,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        User currentUser = authService.getCurrentUserEntity();
        Long effectiveUserId = userId;
        if (!authService.isAdmin()) {
            effectiveUserId = currentUser.getId();
        }

        Page<Bill> billPage = billRepository.searchBills(effectiveUserId, paymentStatus, pageable);
        return PageResponse.from(billPage, b -> BillResponse.fromEntity(b, false));
    }

    public PageResponse<BillResponse> getMyBills(
            PaymentStatus paymentStatus,
            int page,
            int size) {

        User currentUser = authService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Bill> billPage;
        if (paymentStatus != null) {
            billPage = billRepository.findByUserIdAndPaymentStatus(currentUser.getId(), paymentStatus, pageable);
        } else {
            billPage = billRepository.findByUserId(currentUser.getId(), pageable);
        }

        return PageResponse.from(billPage, b -> BillResponse.fromEntity(b, false));
    }

    @Transactional
    public BillResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request) {
        Bill bill = getBillEntityById(id);
        checkModifyPermission(bill);

        PaymentStatus newStatus = request.getPaymentStatus();
        validatePaymentStatusTransition(bill.getPaymentStatus(), newStatus);

        bill.setPaymentStatus(newStatus);

        if (request.getPaymentChannel() != null) {
            bill.setPaymentChannel(request.getPaymentChannel());
        }
        if (request.getPaidAmount() != null) {
            bill.setPaidAmount(request.getPaidAmount());
        }
        if (request.getTransactionId() != null) {
            bill.setTransactionId(request.getTransactionId());
        }
        if (request.getPaymentTime() != null) {
            bill.setPaymentTime(request.getPaymentTime());
        } else if (newStatus == PaymentStatus.PAID) {
            bill.setPaymentTime(LocalDateTime.now());
        }
        if (request.getRemark() != null) {
            bill.setRemark(request.getRemark());
        }

        if (newStatus == PaymentStatus.PAID && bill.getPaidAmount() == null) {
            bill.setPaidAmount(bill.getTotalAmount());
        }

        bill = billRepository.save(bill);
        return BillResponse.fromEntity(bill, true);
    }

    @Transactional
    public BillResponse initiatePayment(Long id, PaymentChannel paymentChannel) {
        Bill bill = getBillEntityById(id);
        checkModifyPermission(bill);

        if (bill.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("当前账单状态不支持发起支付");
        }

        PaymentGateway gateway = paymentGatewayFactory.getGateway(paymentChannel);
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(bill.getId().toString())
                .amount(bill.getTotalAmount())
                .subject("乐器室预约费用")
                .description("预约ID: " + bill.getBooking().getId())
                .paymentChannel(paymentChannel)
                .build();

        PaymentResponse paymentResponse = gateway.createPayment(paymentRequest);

        if (paymentResponse.isSuccess()) {
            bill.setPaymentChannel(paymentChannel);
            bill.setPaymentStatus(paymentResponse.getPaymentStatus());
            bill = billRepository.save(bill);
        }

        return BillResponse.fromEntity(bill, true);
    }

    @Transactional
    public BillResponse processPaymentCallback(Long id) {
        Bill bill = getBillEntityById(id);

        PaymentGateway gateway = paymentGatewayFactory.getGateway(bill.getPaymentChannel());
        PaymentResponse paymentResponse = gateway.queryPayment(bill.getId().toString());

        if (paymentResponse.isSuccess() && paymentResponse.getPaymentStatus() == PaymentStatus.PAID) {
            bill.setPaymentStatus(PaymentStatus.PAID);
            bill.setTransactionId(paymentResponse.getTransactionId());
            bill.setPaymentTime(paymentResponse.getPaymentTime());
            bill.setPaidAmount(paymentResponse.getPaidAmount());
            bill = billRepository.save(bill);
        }

        return BillResponse.fromEntity(bill, true);
    }

    public Bill getBillEntityById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("账单", "id", id));
    }

    private void validateBookingForBilling(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("已取消的预约无法生成账单");
        }
    }

    private void validatePaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == PaymentStatus.PAID && newStatus == PaymentStatus.PENDING) {
            throw new BusinessException("已支付的账单不能改为待支付");
        }
        if (currentStatus == PaymentStatus.REFUNDED && newStatus != PaymentStatus.REFUNDED) {
            throw new BusinessException("已退款的账单不能变更状态");
        }
        if (currentStatus == PaymentStatus.CANCELLED && newStatus != PaymentStatus.CANCELLED) {
            throw new BusinessException("已取消的账单不能变更状态");
        }
    }

    private void checkViewPermission(Bill bill) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(bill.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权查看此账单");
        }
    }

    private void checkModifyPermission(Booking booking) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(booking.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权操作此预约的账单");
        }
    }

    private void checkModifyPermission(Bill bill) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(bill.getUser().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权操作此账单");
        }
    }
}
