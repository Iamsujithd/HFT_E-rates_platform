package com.hft.rates.execution_service.service;

import com.hft.rates.common.dto.TradeRequest;
import com.hft.rates.common.dto.TradeResponse;
import com.hft.rates.execution_service.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TradeProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TradeProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaConfig.TRADE_REQUESTS_TOPIC, groupId = "execution-group")
    public void processTradeRequest(TradeRequest request) {
        if (request.getAmount().compareTo(new BigDecimal("100000000")) > 0) {
            TradeResponse response = TradeResponse.builder()
                    .tradeId(request.getTradeId())
                    .clientId(request.getClientId())
                    .status("REJECTED")
                    .reason("Amount exceeds maximum limit of 100M")
                    .executionTimestamp(Instant.now())
                    .build();
            kafkaTemplate.send(KafkaConfig.TRADE_RESPONSES_TOPIC, request.getClientId(), response);
            return;
        }

        kafkaTemplate.send(KafkaConfig.LEDGER_REQUESTS_TOPIC, request.getTradeId(), request);
    }
}
