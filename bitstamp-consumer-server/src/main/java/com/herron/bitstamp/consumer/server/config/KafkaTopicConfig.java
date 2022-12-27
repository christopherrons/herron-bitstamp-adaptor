package com.herron.bitstamp.consumer.server.config;

import com.herron.bitstamp.consumer.server.enums.TopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic bitstampPartitionOneTopic() {
        return TopicBuilder
                .name(TopicEnum.BITSTAMP_MARKET_DATA.getTopicName())
                .partitions(1)
                .build();
    }
}
