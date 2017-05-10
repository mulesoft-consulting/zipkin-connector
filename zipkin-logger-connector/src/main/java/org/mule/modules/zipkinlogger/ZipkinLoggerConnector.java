package org.mule.modules.zipkinlogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataRetriever;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.OnException;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.MetaDataKeyParam;
import org.mule.api.annotations.param.MetaDataKeyParamAffectsType;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaDataModel;
import org.mule.common.metadata.builder.DefaultMetaDataBuilder;
import org.mule.extension.annotations.param.Ignore;
import org.mule.modules.zipkinlogger.config.ConnectorConfig;
import org.mule.modules.zipkinlogger.error.ErrorHandler;
import org.mule.modules.zipkinlogger.model.BinaryAnnotation;
import org.mule.modules.zipkinlogger.model.HierarchicalLoggerTags;
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

	// TODO describe params
	@Processor
	public brave.Span createAndStartSpan(MuleEvent muleEvent,
			@MetaDataKeyParam(affects = MetaDataKeyParamAffectsType.INPUT) String spanCreationType, @Default("#[payload]") Object spanCreationData,
			@Default(value = "SERVER") Kind spanType, @Default(value = "myspan") String spanName,
			@Default(value = "spanId") String flowVariableToSetWithId) {
		
		// Check if the context is propagated from incoming call
		brave.Span span = createOrJoinSpan(spanCreationData, spanCreationType);

		span.name(spanName).kind(spanType);

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
	 * @param spanCreationData
	 * @param spanCreationType
	 * @return
	 */
	private brave.Span createOrJoinSpan(Object spanCreationData, String spanCreationType) {
		
		if ("standalone_id".equals(spanCreationType)) {
			LoggerTags tagData = (LoggerTags) spanCreationData;
			
			Extractor<LoggerTags> extractor = tracing.propagation().extractor(new Getter<LoggerTags, String>() {
				@Override
				public String get(LoggerTags message, String key) {
					// Don't propagate parent tags
					return null;
				}
			});
			
			TraceContextOrSamplingFlags contextOrFlags = extractor.extract(tagData);
			
			if (contextOrFlags.context() != null) {
				logger.error("Found parent tags, joining spans instead");
				return tracer.joinSpan(contextOrFlags.context());
			} else {
				logger.debug("Starting new span");
				return tracer.newTrace(contextOrFlags.samplingFlags());
			}
		} else if ("join_id".equals(spanCreationType)) {
			HierarchicalLoggerTags tagData = (HierarchicalLoggerTags) spanCreationData;
			
			Extractor<HierarchicalLoggerTags> extractor = tracing.propagation().extractor(new Getter<HierarchicalLoggerTags, String>() {
				@Override
				public String get(HierarchicalLoggerTags message, String key) {
					if ("X-B3-TraceId".equals(key)) {
						return message.getParentInfo().getTraceId();
					} else if ("X-B3-ParentSpanId".equals(key)) {
						return message.getParentInfo().getParentSpanId();
					} else if ("X-B3-SpanId".equals(key)) {
						return message.getParentInfo().getSpanId();
					} else if ("X-B3-Sampled".equals(key)) {
						return message.getParentInfo().getSampled();
					}
					
					return null;
					
				}
			});
			
			TraceContextOrSamplingFlags contextOrFlags = extractor.extract(tagData);
			
			if (contextOrFlags.context() != null) {
				logger.debug("Found parent tags, joining");
				return tracer.joinSpan(contextOrFlags.context());
			} else {
				logger.error("Starting new span, propagation details not found.");
				return tracer.newTrace(contextOrFlags.samplingFlags());
			}
		}
		
		return null;		
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

	@MetaDataKeyRetriever
	public List<MetaDataKey> getKeys() throws Exception {
		List<MetaDataKey> keys = new ArrayList<MetaDataKey>();

		keys.add(new DefaultMetaDataKey("standalone_id", "Standalone Span"));
		keys.add(new DefaultMetaDataKey("join_id", "Join Parent Span"));
		return keys;
	}

	@MetaDataRetriever
	public MetaData getPayloadModel(MetaDataKey entityKey) throws Exception {

		if ("standalone_id".equals(entityKey.getId())) {
			MetaDataModel standaloneModel = new DefaultMetaDataBuilder().createPojo(LoggerTags.class).build();
			return new DefaultMetaData(standaloneModel);
		} else if ("join_id".equals(entityKey.getId())) {
			MetaDataModel joinModel = new DefaultMetaDataBuilder().createPojo(HierarchicalLoggerTags.class).build();
			return new DefaultMetaData(joinModel);
		} else {
			throw new RuntimeException(String.format("This entity %s is not supported", entityKey.getId()));
		}

	}

}