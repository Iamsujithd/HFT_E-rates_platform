package com.hft.rates.service;

import com.hft.rates.common.dto.RateTick;
import com.hft.rates.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final BigDecimal CLIENT_SPREAD = new BigDecimal("0.02");

    public PricingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaConfig.RAW_RATES_TOPIC, groupId = "pricing-engine-group")
    public void processRawTick(RateTick rawTick) {
        BigDecimal clientBid = rawTick.getBidPrice().subtract(CLIENT_SPREAD);
        BigDecimal clientAsk = rawTick.getAskPrice().add(CLIENT_SPREAD);

        RateTick pricedTick = RateTick.builder()
                .instrumentId(rawTick.getInstrumentId())
                .bidPrice(clientBid)
                .askPrice(clientAsk)
                .timestamp(rawTick.getTimestamp())
                .build();

        kafkaTemplate.send(KafkaConfig.PRICED_RATES_TOPIC, pricedTick.getInstrumentId(), pricedTick);
    }
}
