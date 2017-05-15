# zipkin-connector
Zipkin connector for Mule that allows tracing and logging of requests traversing Mule components. Zipkin is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in microservice architectures. It manages both, the collection and lookup of this data. Zipkin’s design is based on the [Google Dapper paper](https://research.google.com/pubs/pub36356.html).

## Overview
Zipkin connector can be used un Mule flows to trace execution of activities, time external callouts, and event logging. Mule tracing can be integrated with tracing in other systems and applications and an end to end performance analysis can be performed. 
Tracing data can be sent to [Zipkin server](https://github.com/openzipkin/zipkin) for analytics and viewing.
Zipkin connector integrates with [brave](https://github.com/openzipkin/brave) Java library to create and manage trace data, and with other [Zipkin libraries](https://github.com/openzipkin) for connecting to reporting server.  

## Terminology
### Span
Span is a single measurement. Synchronous spans have start timestamp and duration, asynchronous spans only have start (point in time) timestamp. Spans have various tags, names, ids, and can link to other spans as parents and children. Parent-child relationship between spans is indicated by `parentSpanId = spanId relationship`.

### Trace

### Parent span

### CLIENT or SERVER?

### Log message and annotations

### Joining external trace

### Joining internal trace

### Synchronous vs. asynchronous spans

## Use cases
### Starting a trace

### Joining externally originated trace

### Joining internally originated trace

### Joining a trace with asynchronous trace

### Creating asynchronous trace

## Installation

## More information
### Zipkin

### Mule example
