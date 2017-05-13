
package com.mulesoft.consulting.zipkinloggerconnector.model;

public class TraceData {

	private String traceId;
	private String spanId;
	private String parentSpanId;
	private String sampled;
	private String debug;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public TraceData() {
	}

	/**
	 * @param traceId
	 * @param spanId
	 * @param parentSpanId
	 * @param sampled
	 * @param debug
	 */
	public TraceData(String traceId, String spanId, String parentSpanId, String sampled, String debug) {
		super();
		this.traceId = traceId;
		this.spanId = spanId;
		this.parentSpanId = parentSpanId;
		this.sampled = sampled;
		this.debug = debug;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
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
