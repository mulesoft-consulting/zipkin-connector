package org.mule.modules.zipkinlogger;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.MetaDataScope;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.OnException;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.MetaDataKeyParam;
import org.mule.extension.annotations.param.Ignore;
import org.mule.modules.zipkinlogger.config.ConnectorConfig;
import org.mule.modules.zipkinlogger.error.ErrorHandler;
import org.mule.modules.zipkinlogger.model.BinaryAnnotation;
import org.mule.modules.zipkinlogger.model.LoggerTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brave.Span.Kind;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.Propagation.Getter;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContextOrSamplingFlags;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Connector(name = "zipkin-logger", friendlyName = "Zipkin Logger")
@MetaDataScope(DataSenseResolver.class)
@OnException(handler = ErrorHandler.class)
public class ZipkinLoggerConnector {

	@Config
	ConnectorConfig config;

	@Inject
	MuleContext muleContext;

	private Tracing tracing;
	private AsyncReporter<Span> reporter;

	private Tracer tracer;
	private Map<Long, brave.Span> spansInFlight = new HashMap<Long, brave.Span>();;

	private static Logger logger = LoggerFactory.getLogger(ZipkinLoggerConnector.class);

	/*
	 * Initialise Zipkin connector
	 */
	@Start
	public void init() {
		// Configure a reporter, which controls how often spans are sent
		// (the dependency is io.zipkin.reporter:zipkin-sender-okhttp3)
		OkHttpSender sender = OkHttpSender.create(config.getZipkinUrl());
		reporter = AsyncReporter.builder(sender).build();

		// Create a tracing component with the service name you want to see in
		// Zipkin.
		tracing = Tracing.newBuilder().localServiceName(config.getServiceName()).reporter(reporter).build();

		// Tracing exposes objects you might need, most importantly the tracer
		tracer = tracing.tracer();

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
		reporter.close();
	}

	/**
	 * Custom processor
	 *
	 * @param muleEvent
	 *            MuleEvent propagated from the flow
	 * @param spanName
	 *            Name for the new root span
	 * @param spanType
	 *            Type of the Span created. Can be CLIENT or SERVER only.
	 * @param ignoreContextPropagation
	 *            Ignore parent span Id propagation from outside and start a new
	 *            span, if true.
	 * @param flowVariableToSetWithId
	 *            name of the flow variable to populate with newly created
	 *            spanId. Will be used for finish.
	 * @param debug
	 *            Ignore the passed value and log the span?
	 * 
	 * @return Original MuleEvent
	 */
	@Processor
	public brave.Span createAndStartSpan(MuleEvent muleEvent, @Default(value = "myspan") String spanName,
			@Default(value = "SERVER") String spanType, @Default(value = "false") boolean ignoreContextPropagation,
			@Default(value = "spanId") String flowVariableToSetWithId) {

		// Initialise the values
		Kind kind = Kind.valueOf(spanType);

		// Check if the context is propagated from incoming call
		brave.Span span = createOrJoinSpan(muleEvent.getMessage(), ignoreContextPropagation);

		span.name(spanName).kind(kind);

		// Get the tags
		extractTags(muleEvent, span);

		span.remoteEndpoint(Endpoint.builder().serviceName(config.getServiceName()).build());

		span.start();

		Long spanId = span.context().spanId();

		// Set the flowVar with created spanId
		muleEvent.setFlowVariable(flowVariableToSetWithId, spanId);

		// Store span for future lookup
		spansInFlight.put(spanId, span);

		return span;
	}

	/*
	 * Finish span. Look up in from the inflight spans by spanId.
	 * 
	 * @param spanId Span Id to finish.
	 */
	@Processor
	public brave.Span finishSpan(@Default(value = "#[flowVars.spanId]") String spanIdExpr) {

		Long spanId = (long) muleContext.getExpressionLanguage().evaluate(spanIdExpr);

		brave.Span span = spansInFlight.remove(spanId);

		if (span != null) {
			span.finish();
		} else {
			logger.warn("Span " + spanId + " not found");
		}

		return span;
	}

	/**
	 * @param muleEvent
	 * @param span
	 */
	private void extractTags(MuleEvent muleEvent, brave.Span span) {

		try {
			LoggerTags tags = (LoggerTags) muleEvent.getMessage().getPayload();

			// Add tags to span
			for (BinaryAnnotation tag : tags.getBinaryAnnotations()) {
				span.tag(tag.getKey(), tag.getValue());
			}
		} catch (ClassCastException e) {
			// Ignore cast error when LoggerTags is not defined in the payload
			logger.debug("Message payload is not LoggerTags. Skipping tags creation.");
		}
	}

	/**
	 * @param muleMessage
	 * @param ignoreContextPropagation
	 * @return
	 */
	private brave.Span createOrJoinSpan(MuleMessage muleMessage, boolean ignoreContextPropagation) {
		Extractor<MuleMessage> extractor = tracing.propagation().extractor(new Getter<MuleMessage, String>() {
			@Override
			public String get(MuleMessage message, String key) {
				logger.debug("Looking for property " + key);
				return message.getInboundProperty(key);
			}
		});

		TraceContextOrSamplingFlags contextOrFlags = extractor.extract(muleMessage);
		
		if (contextOrFlags.context() != null && !ignoreContextPropagation) {
			logger.debug("Joining existing span");
			return tracer.joinSpan(contextOrFlags.context());
		} else {
			logger.debug("Starting new span");
			return tracer.newTrace(contextOrFlags.samplingFlags());
		}
	}

	/**
	 * DataSense processor
	 * 
	 * @param key
	 *            Key to be used to populate the entity
	 * @param entity
	 *            Map that represents the entity
	 * @return Some string
	 */
	@Processor
	public Map<String, Object> addEntity(@MetaDataKeyParam String key,
			@Default("#[payload]") Map<String, Object> entity) {
		/*
		 * USE THE KEY AND THE MAP TO DO SOMETHING
		 */
		return entity;
	}

	public void setConfig(ConnectorConfig config) {
		this.config = config;
	}

	@Processor
	@Ignore
	public void setTracer(Tracer tracer) {
		this.tracer = tracer;
	}

	public void setMuleContext(MuleContext muleContext) {
		this.muleContext = muleContext;
	}

	public ConnectorConfig getConfig() {
		return config;
	}

	/*
	 * @MetaDataKeyRetriever public List<MetaDataKey> getKeys() throws Exception
	 * { return null; }
	 * 
	 * @MetaDataRetriever public MetaData getMetaData(MetaDataKey key) throws
	 * Exception { return null; }
	 */

}