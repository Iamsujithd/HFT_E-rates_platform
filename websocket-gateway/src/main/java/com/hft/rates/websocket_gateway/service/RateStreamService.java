package com.hft.rates.websocket_gateway.service;

import com.hft.rates.common.dto.RateTick;
import com.hft.rates.websocket_gateway.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateStreamService {

    private final SimpMessagingTemplate messagingTemplate;

    public RateStreamService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = KafkaConfig.PRICED_RATES_TOPIC, groupId = "gateway-group")
    public void consumePricedRate(RateTick rateTick) {
        messagingTemplate.convertAndSend("/topic/rates", rateTick);
    }
}
