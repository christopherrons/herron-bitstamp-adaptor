package com.herron.bitstamp.consumer.server.config;

import com.herron.bitstamp.consumer.server.BitstampConsumer;
import com.herron.bitstamp.consumer.server.api.MessageHandler;
import com.herron.bitstamp.consumer.server.messagehandler.DefaultMessageHandler;
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
    public MessageHandler eventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultMessageHandler(kafkaTemplate);
    }

    @Bean(initMethod = "init")
    public BitstampConsumer bitstampConsumer(SubscriptionDetailConfig subscriptionDetailConfig, MessageHandler messageHandler, NewTopic bitstampMarketDataTopic) {
        return new BitstampConsumer(subscriptionDetailConfig, messageHandler);
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
