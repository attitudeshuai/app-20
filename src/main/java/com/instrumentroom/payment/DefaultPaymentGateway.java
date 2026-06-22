package com.instrumentroom.payment;

import com.instrumentroom.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DefaultPaymentGateway implements PaymentGateway {

    @Override
    public String getGatewayName() {
        return "DEFAULT";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        return PaymentResponse.builder()
                .success(true)
                .orderId(request.getOrderId())
                .transactionId("MOCK_" + UUID.randomUUID().toString().replace("-", ""))
                .paymentStatus(PaymentStatus.PENDING)
                .paymentChannel(request.getPaymentChannel())
                .paidAmount(request.getAmount())
                .paymentUrl("https://mock-payment-gateway.com/pay/" + request.getOrderId())
                .build();
    }

    @Override
    public PaymentResponse queryPayment(String orderId) {
        return PaymentResponse.builder()
                .success(true)
                .orderId(orderId)
                .transactionId("MOCK_" + UUID.randomUUID().toString().replace("-", ""))
                .paymentStatus(PaymentStatus.PAID)
                .paymentTime(LocalDateTime.now())
                .build();
    }

    @Override
    public RefundResponse createRefund(RefundRequest request) {
        return RefundResponse.builder()
                .success(true)
                .orderId(request.getOrderId())
                .transactionId(request.getTransactionId())
                .refundId("REFUND_" + UUID.randomUUID().toString().replace("-", ""))
                .paymentStatus(PaymentStatus.REFUNDED)
                .refundAmount(request.getRefundAmount())
                .refundTime(LocalDateTime.now())
                .build();
    }

    @Override
    public RefundResponse queryRefund(String refundId) {
        return RefundResponse.builder()
                .success(true)
                .refundId(refundId)
                .paymentStatus(PaymentStatus.REFUNDED)
                .refundTime(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        return true;
    }
}
