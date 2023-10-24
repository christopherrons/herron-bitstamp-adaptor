package com.herron.exchange.eventgenerator.server.consumers;

import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.api.kafka.KafkaMessageHandler;
import com.herron.exchange.common.api.common.consumer.DataConsumer;
import com.herron.exchange.common.api.common.kafka.KafkaConsumerClient;
import com.herron.exchange.common.api.common.kafka.model.KafkaSubscriptionDetails;
import com.herron.exchange.common.api.common.kafka.model.KafkaSubscriptionRequest;
import com.herron.exchange.common.api.common.messages.BroadcastMessage;
import com.herron.exchange.common.api.common.messages.common.DataStreamState;
import com.herron.exchange.common.api.common.messages.marketdata.entries.MarketDataPrice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class PreviousSettlementPriceConsumer extends DataConsumer implements KafkaMessageHandler {
    private final KafkaConsumerClient consumerClient;
    private final List<KafkaSubscriptionRequest> requests;
    private final Map<String, MarketDataPrice> instrumentIdToPreviousSettlementPrices = new ConcurrentHashMap<>();

    public PreviousSettlementPriceConsumer(KafkaConsumerClient consumerClient, List<KafkaSubscriptionDetails> subscriptionDetails) {
        super("Previous-Settlement-Price", new CountDownLatch(subscriptionDetails.size()));
        this.consumerClient = consumerClient;
        this.requests = subscriptionDetails.stream().map(d -> new KafkaSubscriptionRequest(d, this)).toList();
    }

    @Override
    public void consumerInit() {
        requests.forEach(consumerClient::subscribeToBroadcastTopic);
    }

    @Override
    public void onMessage(BroadcastMessage broadcastMessage) {
        Message message = broadcastMessage.message();
        if (message instanceof DataStreamState state) {
            switch (state.state()) {
                case START -> logger.info("Started consuming previous day settlement price data.");
                case DONE -> {
                    consumerClient.stop(broadcastMessage.partitionKey());
                    countDownLatch.countDown();
                    if (countDownLatch.getCount() == 0) {
                        consumerComplete();
                    }
                }
            }
        } else if (message instanceof MarketDataPrice marketDataPrice) {
            instrumentIdToPreviousSettlementPrices.put(marketDataPrice.staticKey().instrumentId(), marketDataPrice);
        }
    }

    public Map<String, MarketDataPrice> getInstrumentIdToPreviousSettlementPrices() {
        return instrumentIdToPreviousSettlementPrices;
    }


}
