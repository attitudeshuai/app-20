package com.instrumentroom.payment;

import com.instrumentroom.entity.PaymentChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayFactory.class);

    private final Map<PaymentChannel, PaymentGateway> gatewayMap = new EnumMap<>(PaymentChannel.class);
    private final PaymentGateway defaultGateway;

    public PaymentGatewayFactory(List<PaymentGateway> gateways, DefaultPaymentGateway defaultGateway) {
        this.defaultGateway = defaultGateway;
        for (PaymentGateway gateway : gateways) {
            if (gateway instanceof DefaultPaymentGateway) {
                continue;
            }
            String gatewayName = gateway.getGatewayName();
            try {
                PaymentChannel channel = PaymentChannel.valueOf(gatewayName);
                gatewayMap.put(channel, gateway);
                log.info("自动注册支付网关: {} -> {}", channel, gateway.getClass().getSimpleName());
            } catch (IllegalArgumentException e) {
                log.warn("支付网关 '{}' 的名称无法匹配任何 PaymentChannel 枚举值，已跳过", gatewayName);
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
