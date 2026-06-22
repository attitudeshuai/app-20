package com.instrumentroom.payment;

import com.instrumentroom.entity.PaymentChannel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentChannel, PaymentGateway> gatewayMap = new HashMap<>();
    private final PaymentGateway defaultGateway;

    public PaymentGatewayFactory(List<PaymentGateway> gateways, DefaultPaymentGateway defaultGateway) {
        this.defaultGateway = defaultGateway;
        for (PaymentGateway gateway : gateways) {
            if (gateway instanceof DefaultPaymentGateway) {
                continue;
            }
            if (gateway.getGatewayName().equals("ALIPAY")) {
                gatewayMap.put(PaymentChannel.ALIPAY, gateway);
            } else if (gateway.getGatewayName().equals("WECHAT_PAY")) {
                gatewayMap.put(PaymentChannel.WECHAT_PAY, gateway);
            } else if (gateway.getGatewayName().equals("CREDIT_CARD")) {
                gatewayMap.put(PaymentChannel.CREDIT_CARD, gateway);
            } else if (gateway.getGatewayName().equals("BANK_TRANSFER")) {
                gatewayMap.put(PaymentChannel.BANK_TRANSFER, gateway);
            } else if (gateway.getGatewayName().equals("CASH")) {
                gatewayMap.put(PaymentChannel.CASH, gateway);
            }
        }
    }

    public PaymentGateway getGateway(PaymentChannel channel) {
        if (channel == null) {
            return defaultGateway;
        }
        return gatewayMap.getOrDefault(channel, defaultGateway);
    }
}
