package com.mulesoft.consulting.zipkinloggerconnector.model;

public class SpanData {

	private String spanId;
	private String parentSpanId;
	private String traceId;
	private String sampled;
	private String debug;

	public SpanData() {
	}

	public SpanData(String spanId, String parentSpanId, String traceId, String sampled, String debug) {
		super();
		this.spanId = spanId;
		this.parentSpanId = parentSpanId;
		this.traceId = traceId;
		this.sampled = sampled;
		this.debug = debug;
	}

	public String getSpanId() {
		return spanId;
	}

	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}

	public String getParentSpanId() {
		return parentSpanId;
	}

	public void setParentSpanId(String parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getSampled() {
		return sampled;
	}

	public void setSampled(String sampled) {
		this.sampled = sampled;
	}

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}

}