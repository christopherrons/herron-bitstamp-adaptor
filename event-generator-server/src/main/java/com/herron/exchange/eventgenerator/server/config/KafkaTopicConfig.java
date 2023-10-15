package com.herron.exchange.eventgenerator.server.config;

import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.order-data.nr-of-partitions:1}")
    public int nrOfPartitions;

    @Bean
    public NewTopic bitstampMarketDataTopic() {
        return TopicBuilder
                .name(KafkaTopicEnum.ORDER_DATA.getTopicName())
                .partitions(nrOfPartitions)
                .build();
    }
}
