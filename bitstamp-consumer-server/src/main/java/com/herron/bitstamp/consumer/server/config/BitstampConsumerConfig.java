package com.herron.bitstamp.consumer.server.config;

import com.herron.bitstamp.consumer.server.BitstampConsumer;
import com.herron.bitstamp.consumer.server.api.EventHandler;
import com.herron.bitstamp.consumer.server.eventhandler.DefaultEventHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@Import({KafkaProducerConfig.class})
public class BitstampConsumerConfig {

    @Bean
    public EventHandler eventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultEventHandler(kafkaTemplate);
    }

    @Bean(initMethod = "init")
    public BitstampConsumer bitstampConsumer(SubscriptionDetailConfig subscriptionDetailConfig, EventHandler eventHandler) {
        return new BitstampConsumer(subscriptionDetailConfig, eventHandler);
    }

    @Component
    @ConfigurationProperties(prefix = "subscription-config")
    public static class SubscriptionDetailConfig {

        private String uri;
        private final List<SubscriptionDetail> subscriptionDetails = new ArrayList<>();

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public record SubscriptionDetail(String fxCurrency, String cryptoCurrency, String channel) {
        }

        public List<SubscriptionDetail> getSubscriptionDetails() {
            return subscriptionDetails;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubscriptionDetailConfig that)) return false;
            return Objects.equals(subscriptionDetails, that.subscriptionDetails);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subscriptionDetails);
        }
    }

}
