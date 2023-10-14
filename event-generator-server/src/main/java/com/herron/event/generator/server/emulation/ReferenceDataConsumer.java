package com.herron.event.generator.server.emulation;

import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.api.broadcasts.DataLoadingState;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Market;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.kafka.DataConsumer;
import com.herron.exchange.common.api.common.model.PartitionKey;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;

import java.util.concurrent.CountDownLatch;


public class ReferenceDataConsumer extends DataConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataConsumer.class);
    private static final PartitionKey PARTITION_ZERO_KEY = new PartitionKey(KafkaTopicEnum.HERRON_AUDIT_TRAIL, 0);
    private final CountDownLatch countDownLatch;

    public ReferenceDataConsumer(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @KafkaListener(id = "reference-data-consumer-0",
            topicPartitions = {@TopicPartition(topic = "herron-reference-data", partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0"))}
    )
    public void listenReferenceDataPartitionZero(ConsumerRecord<String, String> consumerRecord) {
        var broadCastMessage = deserializeBroadcast(consumerRecord, PARTITION_ZERO_KEY);
        if (broadCastMessage != null) {
            handleMessage(broadCastMessage.message());
        }
    }

    private void handleMessage(Message message) {
        if (message instanceof Market market) {
            ReferenceDataCache.getCache().addMarket(market);

        } else if (message instanceof Product product) {
            ReferenceDataCache.getCache().addProduct(product);

        } else if (message instanceof Instrument instrument) {
            ReferenceDataCache.getCache().addInstrument(instrument);

        } else if (message instanceof OrderbookData orderbookData) {
            ReferenceDataCache.getCache().addOrderbookData(orderbookData);

        } else if (message instanceof DataLoadingState state) {
            switch (state.state()) {
                case START -> LOGGER.info("Started consuming reference data.");
                case DONE -> {
                    var count = countDownLatch.getCount();
                    countDownLatch.countDown();
                    LOGGER.info("Done consuming reference data countdown latch from {} to {}.", count, countDownLatch.getCount());
                }
            }
        }
    }
}
