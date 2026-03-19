package com.hft.rates.execution_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String TRADE_REQUESTS_TOPIC = "trade-requests";
    public static final String LEDGER_REQUESTS_TOPIC = "ledger-requests";
    public static final String TRADE_RESPONSES_TOPIC = "trade-responses";

    @Bean
    public NewTopic ledgerRequestsTopic() {
        return TopicBuilder.name(LEDGER_REQUESTS_TOPIC).partitions(3).replicas(1).build();
    }
}
