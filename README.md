# Bitstamp Consumer

This application subscribes to anonymous order or trades for trading pairs configured in the application,
see [official documentation](https://www.bitstamp.net/websocket/v2/).

## Table of Content

* [Requirements](#requirements): Application requirements.
* [Documentation](#documentation): Further documentation.
* [Configuration](#configuration): How to configure the application.
* [Application DevOps](#application-devops): How to deploy the application.

## Requirements

* Java 17
* Kafka (required with default event handling)

## Documentation

* [Deploy Scripts](docs/deploy-scripts.md): Useful scripts once the application is deployed..

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
default [Event Handler](bitstamp-consumer-server/src/main/java/com/herron/bitstamp/consumer/server/eventhandler/DefaultEventHandler.java)
which sends the events to a `Kafka` broker. The Event Handler bean
can be overriden to alter this behavior.

## Application DevOps

### Staring the Application

Start the application from the root folder by running`./gradlew bootRun`.

### Building the Application

Build the application from the root folder by running `/gradlew packageRelease -PreleaseVersion=<release-version>` to
build and bundle the application jar and relevant deploy
scripts.

### Deploying the Application

The application can be deployed to a remote machine using the `deploy` task.
