package com.hft.rates.ledger_service.repository;

import com.hft.rates.ledger_service.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, String> {
}
