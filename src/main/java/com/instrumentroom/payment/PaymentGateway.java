package com.instrumentroom.payment;

public interface PaymentGateway {

    String getGatewayName();

    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse queryPayment(String orderId);

    RefundResponse createRefund(RefundRequest request);

    RefundResponse queryRefund(String refundId);

    boolean verifyWebhookSignature(String payload, String signature);
}
