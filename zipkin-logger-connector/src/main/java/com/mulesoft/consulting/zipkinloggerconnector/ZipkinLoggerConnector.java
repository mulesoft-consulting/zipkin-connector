package com.mulesoft.consulting.zipkinloggerconnector;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
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
 * @author michaelhyatt
 *
 */
@Connector(name = "zipkin-logger", friendlyName = "Zipkin Logger")
public class ZipkinLoggerConnector {

	private static final String LOGGER_KEY = "log";

	private static final String SPANS_IN_FLIGHT_KEY = "spansInFlight";

	@Config
	AbstractConfig config;

	@Inject
	MuleContext muleContext;

	private Tracing tracing;
	private AsyncReporter<Span> reporter = null;

	private Tracer tracer;

	@Inject
	Registry registry;

	private String serviceName;

	private static Logger logger = LoggerFactory.getLogger(ZipkinLoggerConnector.class);

	@Processor
	public SpanData createNewTrace(MuleEvent muleEvent, @Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default(value = "newSpanId") String flowVariableToSetWithId,
			@Default(value = "mytrace") String traceName) {

		brave.Span span = tracer.newTrace().name(traceName);

		return startSpan(muleEvent, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, span);

	}

	@Processor
	public SpanData joinSpan(MuleEvent muleEvent, @Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default(value = "newSpanId") String flowVariableToSetWithId,
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

		return createAndStartSpanWithParent(muleEvent, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, parentIdLong, spanIdLong, traceIdLong, nullableSampled, debug);

	}

	@Processor
	public SpanData joinExternalSpan(MuleEvent muleEvent, @Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default(value = "spanId") String flowVariableToSetWithId,
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

		return createAndStartSpanWithParent(muleEvent, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, parentIdLong, spanIdLong, traceIdLong, nullableSampled, debug);

	}

	@Processor
	public SpanData finishSpan(@Default(value = "#[flowVars.spanId]") String expressionToGetSpanId) {

		brave.Span span = getSpansInFlight().remove(expressionToGetSpanId);

		if (span != null) {
			span.finish();
			return extractSpanData(span);
		} else {
			throw new RuntimeException("Span " + expressionToGetSpanId + " not found");
		}

	}

	@Processor
	public SpanData createNewTraceAsync(@Optional @Default("#[]") String logMessage,
			@Optional @Default("#[]") Map<String, String> additionalTags,
			@Default(value = "SERVER") Kind ServerOrClientSpanType, @Default(value = "myspan") String spanName,
			@Default(value = "mytrace") String traceName) {

		brave.Span span = tracer.newTrace().name(traceName);

		return startSpanAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName, span);

	}

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

	@Processor
	public Map<String, brave.Span> getSpansInFlight() {
		return registry.get(SPANS_IN_FLIGHT_KEY);
	}

	private SpanData createAndStartSpanWithParent(MuleEvent muleEvent, String logMessage,
			Map<String, String> additionalTags, Kind ServerOrClientSpanType, String spanName,
			String flowVariableToSetWithId, Long parentIdLong, long spanIdLong, long traceIdLong,
			Boolean nullableSampled, Boolean debug) {

		TraceContext parent = TraceContext.newBuilder().parentId(parentIdLong).spanId(spanIdLong).traceId(traceIdLong)
				.sampled(nullableSampled).debug(debug).build();

		brave.Span span = tracer.newChild(parent);

		return startSpan(muleEvent, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, span);
	}

	private SpanData startSpan(MuleEvent muleEvent, String logMessage, Map<String, String> additionalTags,
			Kind ServerOrClientSpanType, String spanName, String flowVariableToSetWithId, brave.Span span) {

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

	public AbstractConfig getConfig() {
		return config;
	}

	public void setConfig(AbstractConfig config) {
		this.config = config;
	}

	public MuleContext getMuleContext() {
		return muleContext;
	}

	public void setMuleContext(MuleContext muleContext) {
		this.muleContext = muleContext;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

}
