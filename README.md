# zipkin-connector
Zipkin connector for Mule that allows tracing and logging of requests traversing Mule components. Zipkin is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in microservice architectures. It manages both, the collection and lookup of this data. Zipkin’s design is based on the [Google Dapper paper](https://research.google.com/pubs/pub36356.html).

## Overview
Zipkin connector can be used un Mule flows to trace execution of activities, time external callouts, and event logging. Mule tracing can be integrated with tracing in other systems and applications and an end to end performance analysis can be performed. 
Tracing data can be sent to [Zipkin server](https://github.com/openzipkin/zipkin) for analytics and viewing.
Zipkin connector integrates with [brave](https://github.com/openzipkin/brave) Java library to create and manage trace data, and with other [Zipkin libraries](https://github.com/openzipkin) for connecting to reporting server.  

## Terminology
For more detailed overview of the philosophy behind Zipkin, refer to [Google Dapper paper](https://research.google.com/pubs/pub36356.html).

### Span
Span is a single measurement. Synchronous (rpc) spans have start timestamp and duration, asynchronous (one-way) spans only have start (point in time) timestamp. Spans have various tags, names, ids, and can link to other spans as parents and children. Parent-child relationship between spans is indicated by `parentSpanId = spanId` relationship.

### Trace
Trace is a collection of spans interlinked between themselves using `parentSpanId = spanId` relationships. Trace represents an end to end call traced through a number of components and dependencies, each contributing spans to it. Top level span in a trace doesn't have a parentSpanId, but other traces linking to it and each other, have the `parentSpanId = spanId` relationships.

### CLIENT or SERVER?
Traces are tagged differently, depending on whether it is a CLIENT or a SERVER sending or receiving a request. CLIENT indicates first sending a request, and then receiving a response. SERVER indicates the opposite, first, receiving a request, and then, sending the response. When a client sends a request, the trace annotation is timestamped with **cs** (client send) at the beginning of the trace, and with **cr** (client receive) at the end of the trace. When a server receives a request at the beginning of the span, it is timestamped with **sr** (server received) and at the end of the unit of work it is timestamped with **ss** (server send). The delta between pairs of **cs/cr**, and **sr/ss** is calculated as time delta of the span. Note, that asynchronous (one-way) spans only have one stamp - **cs** or **sr**, depending on the type of the span being CLIENT or SERVER.

### Log message and annotations
Connector can create log messages (`log` tag) to add logging text to the spans, as well as receive additional tags represented as key/value Map. This allows annotating the spans with custom log information.

### Joining external trace
When trace is originated outside of Mule component, it communicates the required parameters needed for Mule spans to be able to join the trace. With HTTP, it will follow the [B3 propagation rules](https://github.com/openzipkin/b3-propagation), which can be mapped in Mule into the required fields in the connector activity.

### Joining internal trace
When Mule creates a span that needs to join with another parent trace also managed by Mule, it is possible to specify the spanId of the active parent span during the creation of the child span. The connector then will retrieve all the required information about the parent span and will join the new child span to it using `parentSpanId = spanId` relationship.

### Synchronous vs. asynchronous spans
Synchronous (rpc) spans need to be started and finished using two different activities in Mule flow. Synchronous spans indicate a time period with start and duration. Asynchronous (one-way) spans only have one timestamp indicating point in time event, and only need to be triggered using one activity.

## Use cases and examples
### Starting a trace
```xml
		<zipkin-logger:create-new-trace
			config-ref="Zipkin_Logger__Zipkin_HTTP_Logging_Configuration"
			logMessage="This is message 1" spanName="myspan1" traceName="mytrace1"
			ServerOrClientSpanType="CLIENT" doc:name="Start sync trace">
		</zipkin-logger:create-new-trace>
    <!-- store SpanData in a flowVar -->
		<set-variable variableName="newSpanId" value="#[payload]"
			doc:name="Save payload with SpanData in a flowVar" />
...Do some stuff...
		<zipkin-logger:finish-span
			config-ref="Zipkin_Logger__Zipkin_HTTP_Logging_Configuration"
			expressionToGetSpanId="#[flowVars.newSpanId.spanId]" doc:name="Finish Zipkin span #1" />
```

### Joining externally originated trace

### Joining internally originated trace

### Joining a trace with asynchronous trace

### Sending trace propagation data to HTTP endpoint

### Creating asynchronous trace

## Configuration

## Installation
### Mule connector

### Zipkin server

## More information
### Zipkin

### Mule example
