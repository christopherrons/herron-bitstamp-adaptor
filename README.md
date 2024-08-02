# Event Generator

This application broadcasts events that are generator or available from public
sources  [official documentation](https://www.bitstamp.net/websocket/v2/).

## Table of Content

* [Requirements](#requirements): Application requirements.
* [Documentation](#documentation): Further documentation.
* [Events](#events): Events generated.
* [Application DevOps](#application-devops): How to deploy the application.

## Requirements

* Java 21
* [Kafka](https://www.apache.org/dyn/closer.cgi?path=/kafka/3.3.1/kafka_2.13-3.3.1.tgz)
  (required with default event handling)

## Documentation

* [Deploy Scripts](docs/deploy-scripts.md): Useful scripts once the application is deployed.
* [Data Flow](docs/data-flow.md): Visualization of the application data flow.

## Events

When the application is started it starts consuming reference data and previous day settlement prices from Kafka.

### Bistamp

When the application is started Bitstamp trading pairs configured in
the [application.yml](bitstamp-consumer-server/src/main/resources/application.yml) are automatically subscribed to. The
configuration has the following fields:

```yml
subscription-config:
  uri: wss://ws.bitstamp.net
  channels:
    - live_orders_btcusd
```

### Emulation

Once the reference data and previous day settlement prices are loaded insert orders are generated for max 20 price
levels per orderbook. Tens of thousands of orders are generator per second.

## Application DevOps

### Staring the Application

Start the application from the root folder by running`./gradlew bootRun`.

### Building the Application

Build the application from the root folder by running `/gradlew packageRelease -PreleaseVersion=<release-version>` to
build and bundle the application jar and relevant deploy
scripts.

### Deploying the Application

The application can be deployed to a remote machine using the `deploy` task.
