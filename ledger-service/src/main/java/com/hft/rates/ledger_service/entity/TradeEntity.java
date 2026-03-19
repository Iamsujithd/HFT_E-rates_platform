package com.hft.rates.ledger_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {
    @Id
    private String tradeId;
    private String clientId;
    private String instrumentId;
    private String side;
    private BigDecimal amount;
    private BigDecimal executedPrice;
    private String status;
    private Instant executionTimestamp;
}
