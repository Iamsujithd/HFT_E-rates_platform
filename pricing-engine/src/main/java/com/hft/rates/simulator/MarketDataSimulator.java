package com.hft.rates.simulator;

import com.hft.rates.common.dto.RateTick;
import com.hft.rates.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Component
public class MarketDataSimulator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();
    private final List<String> instruments = List.of("US_10Y", "US_5Y", "US_2Y", "EUR_BUND");

    public MarketDataSimulator(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // High frequency simulation - every 10ms
    @Scheduled(fixedRate = 10)
    public void generateTick() {
        String instrument = instruments.get(random.nextInt(instruments.size()));
        double basePrice = getBasePrice(instrument);
        
        // Random walk
        double change = (random.nextDouble() - 0.5) * 0.01;
        BigDecimal newMid = BigDecimal.valueOf(basePrice + change).setScale(4, RoundingMode.HALF_UP);
        
        BigDecimal bid = newMid.subtract(BigDecimal.valueOf(0.005));
        BigDecimal ask = newMid.add(BigDecimal.valueOf(0.005));

        RateTick tick = RateTick.builder()
                .instrumentId(instrument)
                .bidPrice(bid)
                .askPrice(ask)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(KafkaConfig.RAW_RATES_TOPIC, instrument, tick);
    }

    private double getBasePrice(String instrument) {
        return switch (instrument) {
            case "US_10Y" -> 98.50;
            case "US_5Y" -> 99.10;
            case "US_2Y" -> 99.80;
            case "EUR_BUND" -> 101.20;
            default -> 100.00;
        };
    }
}
