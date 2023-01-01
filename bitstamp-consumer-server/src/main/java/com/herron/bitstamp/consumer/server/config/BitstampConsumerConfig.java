package com.herron.bitstamp.consumer.server.config;

import com.herron.bitstamp.consumer.server.BitstampConsumer;
import com.herron.bitstamp.consumer.server.api.EventHandler;
import com.herron.bitstamp.consumer.server.eventhandler.DefaultEventHandler;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BitstampConsumerConfig {
    @Bean
    public EventHandler eventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultEventHandler(kafkaTemplate);
    }

    @Bean(initMethod = "init")
    public BitstampConsumer bitstampConsumer(SubscriptionDetailConfig subscriptionDetailConfig, EventHandler eventHandler, NewTopic bitstampMarketDataTopic) {
        return new BitstampConsumer(subscriptionDetailConfig, eventHandler);
    }

    @Component
    @ConfigurationProperties(prefix = "subscription-config")
    public static class SubscriptionDetailConfig {

        private String uri;
        private final List<String> channels = new ArrayList<>();

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public List<String> getChannels() {
            return channels;
        }

    }

}
