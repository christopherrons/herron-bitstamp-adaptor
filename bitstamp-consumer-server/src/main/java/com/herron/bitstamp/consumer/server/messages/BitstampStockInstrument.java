package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.enums.InstrumentTypeEnum;
import com.herron.exchange.common.api.common.enums.MessageTypesEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BitstampStockInstrument(String instrumentId,
                                      long timeStampInMs) implements Instrument {

    public BitstampStockInstrument(BitstampStockInstrument instrument) {
        this(instrument.instrumentId(),
                instrument.timeStampInMs());
    }

    @Override
    public BitstampStockInstrument getCopy() {
        return new BitstampStockInstrument(this);
    }

    @Override
    public MessageTypesEnum messageType() {
        return MessageTypesEnum.BITSTAMP_STOCK_INSTRUMENT;
    }

    @Override
    public InstrumentTypeEnum instrumentType() {
        return InstrumentTypeEnum.STOCK;
    }

}
