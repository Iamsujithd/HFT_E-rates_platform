package com.hft.rates.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequest {
    private String tradeId;
    private String clientId;
    private String instrumentId;
    private String side; // BUY or SELL
    private BigDecimal amount;
    private BigDecimal requestedPrice;
    private Instant requestTimestamp;
}
