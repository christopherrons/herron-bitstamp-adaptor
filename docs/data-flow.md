# Data Flow

This document describes the data flow of the application.

## Table of Content

* [Visual Flow Chart](#visual-flow-chart): Visual Flow Chart.

## Visual Flow Chart
```mermaid
flowchart LR;
BSC{Server Starts} --->|WEBSOCKET| BS
BSC{Server Starts} --->|KAFKA|KT

subgraph KT[Kafka Topics]
K1[Reference Data] -.- K2[Prev Settlement Price]
end


subgraph BS[Bitstamp Consumer]
TP1[Trading Pair 1] -.- TPN[Trading Pair... N]
end

subgraph EM[Emulator]
GO[Generator Orders]
end

KT -->|TRIGGER|EM
EM -->|Deserialize|KP
BS -->|Deserialize|KP
subgraph KP[Kafka Producer]
end
```
