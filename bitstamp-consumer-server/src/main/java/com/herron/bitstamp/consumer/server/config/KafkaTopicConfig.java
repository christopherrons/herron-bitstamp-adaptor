package com.herron.bitstamp.consumer.server.config;

import com.herron.exchange.common.api.common.enums.TopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${audit-trail.nr-of-partitions}")
    public int nrOfPartitions;

    @Bean
    public NewTopic bitstampMarketDataTopic() {
        return TopicBuilder
                .name(TopicEnum.BITSTAMP_MARKET_DATA.getTopicName())
                .partitions(nrOfPartitions)
                .build();
    }
}
