# zipkin-connector
Zipkin connector for Mule that allows tracing and logging of requests traversing Mule components. Zipkin is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in microservice architectures. It manages both, the collection and lookup of this data. Zipkin’s design is based on the [Google Dapper paper](https://research.google.com/pubs/pub36356.html).

## Overview
Zipkin connector can be used un Mule flows to trace execution of activities, time external callouts, and event logging. Mule tracing can be integrated with tracing in other systems and applications and an end to end performance analysis can be performed. 
Tracing data can be sent to [Zipkin server](https://github.com/openzipkin/zipkin) for analytics and viewing.
Zipkin connector integrates with [brave](https://github.com/openzipkin/brave) Java library to create and manage trace data, and with other [Zipkin libraries](https://github.com/openzipkin) for connecting to reporting server.  

## Terminology
### Span
Span is a single measurement. Synchronous (rpc) spans have start timestamp and duration, asynchronous (one-way) spans only have start (point in time) timestamp. Spans have various tags, names, ids, and can link to other spans as parents and children. Parent-child relationship between spans is indicated by `parentSpanId = spanId` relationship.

### Trace
Trace is a collection of spans interlinked between themselves using `parentSpanId = spanId` relationships. Trace represents an end to end call traced through a number of components and dependencies, each contributing spans to it. Top level span in a trace doesn't have a parentSpanId, but other traces linking to it and each other, have the `parentSpanId = spanId` relationships.

### CLIENT or SERVER?
Traces are tagged differently, depending on whether it is a CLIENT or a SERVER sending or receiving a request. CLIENT indicates first sending a request, and then receiving a response. SERVER indicates the opposite, first, receiving a request, and then, sending the response. When a client sends a request, the trace annotation is timestamped with **cs** (client send) at the beginning of the trace, and with **cr** (client receive) at the end of the trace. When a server receives a request at the beginning of the span, it is timestamped with **sr** (server received) and at the end of the unit of work it is timestamped with **ss** (server send). The delta between pairs of **cs/cr**, and **sr/ss** is calculated as time delta of the span. Note, that asynchronous (one-way) spans only have one stamp - **cs** or **sr**, depending on the type of the span being CLIENT or SERVER.

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
