package com.hft.rates.websocket_gateway.controller;

import com.hft.rates.common.dto.TradeRequest;
import com.hft.rates.websocket_gateway.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Controller
public class TradeController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TradeController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @MessageMapping("/trade")
    public void handleTradeRequest(TradeRequest request) {
        if (request.getTradeId() == null) {
            request.setTradeId(UUID.randomUUID().toString());
        }
        if (request.getRequestTimestamp() == null) {
            request.setRequestTimestamp(Instant.now());
        }
        
        kafkaTemplate.send(KafkaConfig.TRADE_REQUESTS_TOPIC, request.getClientId(), request);
    }
}
