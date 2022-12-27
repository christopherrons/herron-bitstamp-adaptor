# Bitstamp Consumer

This application subscribes to anonymous order or trades for trading pairs configured in the application,
see [official documentation](https://www.bitstamp.net/websocket/v2/).

## Table of Content

* [Requirements](#requirements): Application requirements.
* [Configuration](#configuration): How to configure the application.
* [Local Development](#local-development): How to develop locally.
* [Deploy](#deploy): How to deploy the application.

## Requirements

* Java 17
* Kafka (required with default event handling)

## Configuration

When the application is started trading pairs configured in
the [application.yml](bitstamp-consumer-server/src/main/resources/application.yml) are automatically subscribed to. The
format is:

```yml
subscription-config:
  subscription-details:
    - fx-currency: eur
      crypto-currency: btc
      channel: live_orders
      uri: wss://ws.bitstamp.net
    - fx-currency: usd
      crypto-currency: btc
      channel: live_orders
      uri: wss://ws.bitstamp.net
    
```

All events are handle by a
default [Event Handler](bitstamp-consumer-server/src/main/java/com/herron/bitstamp/consumer/server/eventhandler/EventHandler.java)
which sends the events to a `Kafka` broker. The Event Handler bean
can be overriden to alter this behavior.

## Local Development

The application can be run using the gradle wrapper or Intellij Idea run configurations.

## Deploy

How to deploy...