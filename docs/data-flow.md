# Data Flow

This document describes the data flow of the application.

## Table of Content

* [Visual Flow Chart](#visual-flow-chart): Visual Flow Chart.

## Visual Flow Chart
```mermaid
flowchart LR;
    BSC{Bitstamp Consumer Server Starta} ---> |websocket| BS
    subgraph BS[Bitstamp Consumer]
        TP1[Trading Pair 1] -.- TPN[Trading Pair... N]
    end
    
    BS --> |Deserialize| KP
    subgraph KP[Kafka Producer]
    end
```
