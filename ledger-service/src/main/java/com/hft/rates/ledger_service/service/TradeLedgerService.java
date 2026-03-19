package com.hft.rates.ledger_service.service;

import com.hft.rates.common.dto.TradeRequest;
import com.hft.rates.common.dto.TradeResponse;
import com.hft.rates.ledger_service.config.KafkaConfig;
import com.hft.rates.ledger_service.entity.TradeEntity;
import com.hft.rates.ledger_service.repository.TradeRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TradeLedgerService {

    private final TradeRepository tradeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TradeLedgerService(TradeRepository tradeRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.tradeRepository = tradeRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = KafkaConfig.LEDGER_REQUESTS_TOPIC, groupId = "ledger-group")
    public void processAndRecordTrade(TradeRequest request) {
        TradeEntity trade = TradeEntity.builder()
                .tradeId(request.getTradeId())
                .clientId(request.getClientId())
                .instrumentId(request.getInstrumentId())
                .side(request.getSide())
                .amount(request.getAmount())
                .executedPrice(request.getRequestedPrice())
                .status("ACCEPTED")
                .executionTimestamp(Instant.now())
                .build();
                
        tradeRepository.save(trade);

        TradeResponse response = TradeResponse.builder()
                .tradeId(trade.getTradeId())
                .clientId(trade.getClientId())
                .status("ACCEPTED")
                .executedPrice(trade.getExecutedPrice())
                .executionTimestamp(trade.getExecutionTimestamp())
                .build();

        kafkaTemplate.send(KafkaConfig.TRADE_RESPONSES_TOPIC, response.getClientId(), response);
    }
}
