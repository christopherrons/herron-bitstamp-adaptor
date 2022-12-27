package com.herron.bitstamp.consumer.server.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;

@ClientEndpoint
public final class CustomClientEndpoint extends Endpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomClientEndpoint.class);
    private final MessageHandler messageHandler;

    public CustomClientEndpoint(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        LOGGER.info("Session opened!");
        if (!session.getMessageHandlers().contains(messageHandler)) {
            session.addMessageHandler(messageHandler);
        }
    }
}
