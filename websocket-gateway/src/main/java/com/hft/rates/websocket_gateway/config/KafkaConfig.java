package com.hft.rates.websocket_gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String PRICED_RATES_TOPIC = "priced-rates";
    public static final String TRADE_REQUESTS_TOPIC = "trade-requests";
    public static final String TRADE_RESPONSES_TOPIC = "trade-responses";

    @Bean
    public NewTopic tradeRequestsTopic() {
        return TopicBuilder.name(TRADE_REQUESTS_TOPIC).partitions(3).replicas(1).build();
    }
    
    @Bean
    public NewTopic tradeResponsesTopic() {
        return TopicBuilder.name(TRADE_RESPONSES_TOPIC).partitions(3).replicas(1).build();
    }
}
