package com.hft.rates.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String RAW_RATES_TOPIC = "raw-rates";
    public static final String PRICED_RATES_TOPIC = "priced-rates";

    @Bean
    public NewTopic rawRatesTopic() {
        return TopicBuilder.name(RAW_RATES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pricedRatesTopic() {
        return TopicBuilder.name(PRICED_RATES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
