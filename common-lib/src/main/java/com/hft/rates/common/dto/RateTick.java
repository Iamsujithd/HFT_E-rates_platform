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
public class RateTick {
    private String instrumentId;   // e.g., "US_10Y"
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Instant timestamp;
}
