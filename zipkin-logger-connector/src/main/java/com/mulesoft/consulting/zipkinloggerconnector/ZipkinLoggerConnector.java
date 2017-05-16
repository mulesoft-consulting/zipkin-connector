package com.mulesoft.consulting.zipkinloggerconnector;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.registry.RegistrationException;
import org.mule.api.registry.Registry;
import org.mule.extension.annotations.param.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mulesoft.consulting.zipkinloggerconnector.config.AbstractConfig;
import com.mulesoft.consulting.zipkinloggerconnector.config.ZipkinConsoleConnectorConfig;
import com.mulesoft.consulting.zipkinloggerconnector.config.ZipkinHttpConnectorConfig;
import com.mulesoft.consulting.zipkinloggerconnector.model.SpanData;

import brave.Span.Kind;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.okhttp3.OkHttpSender;

/**
 * Zipkin is a distributed tracing system. It helps gather timing data needed to
 * troubleshoot latency problems in microservice architectures. It manages both
 * the collection and lookup of this data. Zipkin’s design is based on the
 * Google Dapper paper.
 * 
 * @see <a href="http://zipkin.io/">Zipkin</a>
 * @see <a href="https://github.com/openzipkin/zipkin">Zipkin Github repo</a>
 * @see <a href="https://github.com/openzipkin/brave">Zipkin brave Java client
 *      library</a>
 * @see <a href="https://research.google.com/pubs/pub36356.html">Dapper
 *      paper</a>
 * 
 * @author michaelhyatt
 *
 */
@Connector(name = "zipkin-logger", friendlyName = "Zipkin Logger", minMuleVersion = "3.8.0")
public class ZipkinLoggerConnector {

	@Config
	AbstractConfig config;

	@Inject
	Registry registry;

	private static final String LOGGER_KEY = "log";
	private static final String FINISH_LOGGER_KEY = "finish-log";

	private static final String SPANS_IN_FLIGHT_KEY = "spansInFlight";
	private static Logger logger = LoggerFactory.getLogger(ZipkinLoggerConnector.class);

	private Tracing tracing;
	private AsyncReporter<Span> reporter = null;
	private Tracer tracer;
	private String serviceName;

	/**
	 * Creates new synchronous trace and new parent span. Requires subsequent
	 * {@link finish-span} element to complete and publish the trace.
	 * 
	 * @param logMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "log"
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param ServerOrClientSpanType
	 *            indicator whether the span is logged as SERVER or CLIENT
	 *            brave.span.Kind
	 * @param spanName
	 *            name of the span
	 * @param traceName
	 *            name of the trace
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData createNewTrace(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default(value = "mytrace") String traceName) {

		brave.Span span = tracer.newTrace().name(traceName);

		return startSpan(logMessage, additionalTags, ServerOrClientSpanType, spanName, span);

	}

	/**
	 * Creates new span and adds it to the internally managed parent span that
	 * has to exist. Requires subsequent {@link finish-span} element to complete
	 * and publish the trace.
	 * 
	 * @param logMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "log"
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param ServerOrClientSpanType
	 *            indicator whether the span is logged as SERVER or CLIENT
	 *            brave.span.Kind
	 * @param spanName
	 *            name of the span
	 * @param parentSpanId
	 *            spanId of the internally managed span to join
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData joinSpan(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default("#[flowVars.spanId]") String parentSpanId) {

		brave.Span parentSpan = getSpansInFlight().get(parentSpanId);

		if (parentSpan == null) {
			throw new RuntimeException("Span " + parentSpanId + " not found");
		}

		Long parentIdLong = parentSpan.context().parentId();
		long spanIdLong = parentSpan.context().spanId();
		long traceIdLong = parentSpan.context().traceId();
		Boolean nullableSampled = parentSpan.context().sampled();
		Boolean debug = parentSpan.context().debug();

		return createAndStartSpanWithParent(logMessage, additionalTags, ServerOrClientSpanType, spanName, parentIdLong,
				spanIdLong, traceIdLong, nullableSampled, debug);

	}

	/**
	 * Creates new span and adds it to the externally originated parent span
	 * known through its B3 propagation attributes. Requires subsequent
	 * {@link finish-span} element to complete and publish the trace.
	 * 
	 * @param logMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "log"
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param ServerOrClientSpanType
	 *            indicator whether the span is logged as SERVER or CLIENT
	 *            brave.span.Kind
	 * @param spanName
	 *            name of the span
	 * @param spanId
	 *            spanId of external span to join
	 * @param parentSpanId
	 *            parentSpanId of external span to join
	 * @param traceId
	 *            traceId of external span to join
	 * @param sampled
	 *            sampled value ("1" or "0") of external span to join
	 * @param flags
	 *            flags (debug - "1" or "0") of external span to join
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData joinExternalSpan(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default("#[message.inboundProperties.'x-b3-spanid']") String spanId,
			@Default("#[message.inboundProperties.'x-b3-parentspanid']") String parentSpanId,
			@Default("#[message.inboundProperties.'x-b3-traceid']") String traceId,
			@Optional @Default("#[message.inboundProperties.'x-b3-sampled']") String sampled,
			@Optional @Default("#[message.inboundProperties.'x-b3-flags']") String flags) {

		Long parentIdLong = Long.parseUnsignedLong(parentSpanId, 16);
		long spanIdLong = Long.parseUnsignedLong(spanId, 16);
		long traceIdLong = Long.parseUnsignedLong(traceId, 16);
		Boolean nullableSampled = null;

		if (sampled != null && !sampled.equals(""))
			if (sampled.equals("1"))
				nullableSampled = true;
			else if (sampled.equals("0"))
				nullableSampled = false;

		Boolean debug = false;
		if (flags != null && !flags.equals(""))
			if (flags.equals("1"))
				debug = true;

		return createAndStartSpanWithParent(logMessage, additionalTags, ServerOrClientSpanType, spanName, parentIdLong,
				spanIdLong, traceIdLong, nullableSampled, debug);

	}

	/**
	 * Finishes an internally managed synchronous span known by its spanId
	 * 
	 * @param expressionToGetSpanId
	 *            spanId to finish
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param quietSpanNotFound
	 *            Do not throw an exception, if the span is not found
	 * @param finishSpanLogMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "finish-log"
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData finishSpan(@Default(value = "#[flowVars.spanId]") String expressionToGetSpanId,
			@Optional @Default("#[]") String finishSpanLogMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags, @Default("false") Boolean quietSpanNotFound) {

		brave.Span span = getSpansInFlight().remove(expressionToGetSpanId);

		if (span == null)
			if (quietSpanNotFound)
				return null;
			else
				throw new RuntimeException("Span " + expressionToGetSpanId + " not found");

		// Create logging record and get the tags
		if (finishSpanLogMessage != null && !finishSpanLogMessage.equals(""))
			span.tag(FINISH_LOGGER_KEY, finishSpanLogMessage);

		if (additionalTags != null)
			for (String key : additionalTags.keySet()) {
				span.tag(key, additionalTags.get(key));
			}

		span.finish();
		return extractSpanData(span);
	}

	/**
	 * Creates new trace and an asynchronous span. The trace and the span are
	 * submitted immediately without the need for subsequent finish-span
	 * element.
	 * 
	 * @param logMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "log"
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param ServerOrClientSpanType
	 *            indicator whether the span is logged as SERVER or CLIENT
	 *            brave.span.Kind
	 * @param spanName
	 *            name of the span
	 * @param traceName
	 *            name of the trace
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData createNewTraceAsync(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default(value = "mytrace") String traceName) {

		brave.Span span = tracer.newTrace().name(traceName);

		return startSpanAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName, span);

	}

	/**
	 * Creates new asynchronous span and adds it to the internally managed
	 * parent span that has to exist. The trace and the span are submitted
	 * immediately without the need for subsequent finish-span element.
	 * 
	 * @param logMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "log"
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param ServerOrClientSpanType
	 *            indicator whether the span is logged as SERVER or CLIENT
	 *            brave.span.Kind
	 * @param spanName
	 *            name of the span
	 * @param parentSpanId
	 *            spanId of an internally managed parent span
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData joinSpanAsync(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default("#[flowVars.spanId]") String parentSpanId) {

		brave.Span parentSpan = getSpansInFlight().get(parentSpanId);

		if (parentSpan == null) {
			throw new RuntimeException("Span " + parentSpanId + " not found");
		}

		Long parentIdLong = parentSpan.context().parentId();
		long spanIdLong = parentSpan.context().spanId();
		long traceIdLong = parentSpan.context().traceId();
		Boolean nullableSampled = parentSpan.context().sampled();
		Boolean debug = parentSpan.context().debug();

		return createAndStartSpanAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName, parentIdLong,
				spanIdLong, traceIdLong, nullableSampled, debug);
	}

	/**
	 * Creates new asynchronous span and adds it to the externally originated
	 * parent span that is sent using B3 proapgation attributes. The trace and
	 * the span are submitted immediately without the need for subsequent
	 * finish-span element.
	 * 
	 * @param logMessage
	 *            Logged message that will be added as one of the tags with name
	 *            "log"
	 * @param additionalTags
	 *            additional map of tags to add as Map
	 * @param ServerOrClientSpanType
	 *            indicator whether the span is logged as SERVER or CLIENT
	 *            brave.span.Kind
	 * @param spanName
	 *            name of the span
	 * @param spanId
	 *            spanId of external span to join
	 * @param parentSpanId
	 *            parentSpanId of external span to join
	 * @param traceId
	 *            traceId of external span to join
	 * @param sampled
	 *            sampled value ("1" or "0") of external span to join
	 * @param flags
	 *            flags (debug - "1" or "0") of external span to join
	 * @return payload with {@link SpanData} POJO
	 */
	@Processor
	public SpanData joinExternalSpanAsync(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default("#[message.inboundProperties.'x-b3-spanid']") String spanId,
			@Default("#[message.inboundProperties.'x-b3-parentspanid']") String parentSpanId,
			@Default("#[message.inboundProperties.'x-b3-traceid']") String traceId,
			@Optional @Default("#[message.inboundProperties.'x-b3-sampled']") String sampled,
			@Optional @Default("#[message.inboundProperties.'x-b3-flags']") String flags) {

		Long parentIdLong = Long.parseUnsignedLong(parentSpanId, 16);
		long spanIdLong = Long.parseUnsignedLong(spanId, 16);
		long traceIdLong = Long.parseUnsignedLong(traceId, 16);
		Boolean nullableSampled = null;

		if (sampled != null && !sampled.equals(""))
			if (sampled.equals("1"))
				nullableSampled = true;
			else if (sampled.equals("0"))
				nullableSampled = false;

		Boolean debug = false;
		if (flags != null && !flags.equals(""))
			if (flags.equals("1"))
				debug = true;

		return createAndStartSpanAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName, parentIdLong,
				spanIdLong, traceIdLong, nullableSampled, debug);
	}

	/*
	 * Initialise Zipkin connector
	 */
	@Start
	public void init() {
		if (config instanceof ZipkinHttpConnectorConfig) {

			ZipkinHttpConnectorConfig httpConfig = (ZipkinHttpConnectorConfig) config;
			// Configure a reporter, which controls how often spans are sent
			// (the dependency is io.zipkin.reporter:zipkin-sender-okhttp3)
			OkHttpSender sender = OkHttpSender.create(httpConfig.getZipkinUrl());
			reporter = (AsyncReporter<Span>) AsyncReporter.builder(sender).build();

			// Create a tracing component with the service name you want to see
			// in Zipkin.
			tracing = Tracing.newBuilder().localServiceName(httpConfig.getServiceName()).reporter(reporter).build();

			serviceName = httpConfig.getServiceName();

			logger.debug("Instantiated " + ZipkinHttpConnectorConfig.class.getName());
		} else if (config instanceof ZipkinConsoleConnectorConfig) {
			Reporter<Span> reporter = Reporter.CONSOLE;

			ZipkinConsoleConnectorConfig consoleConfig = (ZipkinConsoleConnectorConfig) config;
			tracing = Tracing.newBuilder().localServiceName(consoleConfig.getServiceName()).reporter(reporter).build();

			serviceName = consoleConfig.getServiceName();

			logger.debug("Instantiated " + ZipkinConsoleConnectorConfig.class.getName());
		} else {
			throw new RuntimeException("Unknown configuration option");
		}

		// Tracing exposes objects you might need, most importantly the
		// tracer
		tracer = tracing.tracer();

		Map<String, brave.Span> spansInFlight = new HashMap<String, brave.Span>();

		try {
			registry.registerObject(SPANS_IN_FLIGHT_KEY, spansInFlight);
		} catch (RegistrationException e) {
			throw new RuntimeException("Unable to store spansInFlight map", e);
		}
	}

	/*
	 * Shutdown Zipkin connector
	 */
	@Stop
	public void shutdown() {
		// When all tracing tasks are complete, close the tracing component and
		// reporter
		// This might be a shutdown hook for some users
		tracing.close();

		if (reporter != null)
			reporter.close();

		logger.debug("ZipkinLogger shutdown called");
	}

	private SpanData createAndStartSpanWithParent(String logMessage, Map<String, String> additionalTags,
			Kind ServerOrClientSpanType, String spanName, Long parentIdLong, long spanIdLong, long traceIdLong,
			Boolean nullableSampled, Boolean debug) {

		TraceContext parent = TraceContext.newBuilder().parentId(parentIdLong).spanId(spanIdLong).traceId(traceIdLong)
				.sampled(nullableSampled).debug(debug).build();

		brave.Span span = tracer.newChild(parent);

		return startSpan(logMessage, additionalTags, ServerOrClientSpanType, spanName, span);
	}

	private SpanData startSpan(String logMessage, Map<String, String> additionalTags, Kind ServerOrClientSpanType,
			String spanName, brave.Span span) {

		span.name(spanName).kind(ServerOrClientSpanType);

		// Create logging record and get the tags
		if (logMessage != null && !logMessage.equals(""))
			span.tag(LOGGER_KEY, logMessage);

		if (additionalTags != null)
			for (String key : additionalTags.keySet()) {
				span.tag(key, additionalTags.get(key));
			}

		String newSpanId = Long.toHexString(span.context().spanId());

		span.remoteEndpoint(Endpoint.builder().serviceName(serviceName).build());

		span.start();

		// Store span for future lookup
		getSpansInFlight().put(newSpanId, span);

		return extractSpanData(span);
	}

	private SpanData extractSpanData(brave.Span span) {

		String spanId = Long.toHexString(span.context().spanId());
		String parentSpanId = span.context().parentId() != null ? Long.toHexString(span.context().parentId())
				: Long.toHexString(span.context().spanId());
		String traceId = Long.toHexString(span.context().traceId());
		String sampled = span.context().sampled() != null ? (span.context().sampled() ? "1" : "0") : null;
		String flags = span.context().debug() ? "1" : "0";
		return new SpanData(spanId, parentSpanId, traceId, sampled, flags);
	}

	private SpanData createAndStartSpanAsync(String logMessage, Map<String, String> additionalTags,
			Kind ServerOrClientSpanType, String spanName, Long parentIdLong, long spanIdLong, long traceIdLong,
			Boolean nullableSampled, Boolean debug) {

		TraceContext parent = TraceContext.newBuilder().parentId(parentIdLong).spanId(spanIdLong).traceId(traceIdLong)
				.sampled(nullableSampled).debug(debug).build();

		brave.Span span = tracer.newChild(parent);

		return startSpanAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName, span);
	}

	private SpanData startSpanAsync(String logMessage, Map<String, String> additionalTags, Kind ServerOrClientSpanType,
			String spanName, brave.Span span) {
		span.name(spanName).kind(ServerOrClientSpanType);

		// Create logging record and get the tags
		if (logMessage != null && !logMessage.equals(""))
			span.tag(LOGGER_KEY, logMessage);

		for (String key : additionalTags.keySet()) {
			span.tag(key, additionalTags.get(key));
		}

		span.remoteEndpoint(Endpoint.builder().serviceName(serviceName).build());

		span.start().flush();

		return extractSpanData(span);
	}

	public Map<String, brave.Span> getSpansInFlight() {
		return registry.get(SPANS_IN_FLIGHT_KEY);
	}

	public AbstractConfig getConfig() {
		return config;
	}

	public void setConfig(AbstractConfig config) {
		this.config = config;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

}
