package com.hft.rates.websocket_gateway.service;

import com.hft.rates.common.dto.TradeResponse;
import com.hft.rates.websocket_gateway.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TradeResponseService {

    private final SimpMessagingTemplate messagingTemplate;

    public TradeResponseService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = KafkaConfig.TRADE_RESPONSES_TOPIC, groupId = "gateway-response-group")
    public void consumeTradeResponse(TradeResponse response) {
        messagingTemplate.convertAndSend("/topic/trades/" + response.getClientId(), response);
    }
}
