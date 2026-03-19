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
public class TradeResponse {
    private String tradeId;
    private String clientId;
    private String status; // ACCEPTED, REJECTED
    private String reason;
    private BigDecimal executedPrice;
    private Instant executionTimestamp;
}
