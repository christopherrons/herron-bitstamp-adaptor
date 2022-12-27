# Bitstamp Consumer

This application subscribes to anonymous order or trades for configured trading pairs,
see [official documentation](https://www.bitstamp.net/websocket/v2/).

## Table of Content

* [Configuration](#configuration): How to configure the application.
* [Deploy](#deploy): How to deploy the application.

## Configuration

When the application is started trading pairs configured in
the [application.yml](bitstamp-consumer-server/src/main/resources/application.yml). The format is:

```yml
subscription-config:
  subscription-details:
    - fx-currency: eur
      crypto-currency: btc
      channel: live_orders
      uri: wss://ws.bitstamp.net
```

## Deploy

How to deploy...