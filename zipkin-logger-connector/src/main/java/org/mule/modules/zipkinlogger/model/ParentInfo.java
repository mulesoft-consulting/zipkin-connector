package org.mule.modules.zipkinlogger.model;

public class ParentInfo {

	private String traceId;
	private String parentSpanId;
	private String spanId;
	private String sampled;
	private String debug;

	public ParentInfo(String traceId, String parentSpanId, String spanId, String sampled) {
		this.traceId = traceId;
		this.parentSpanId = parentSpanId;
		this.spanId = spanId;
		this.sampled = sampled;
	}

	public ParentInfo(String traceId, String parentSpanId, String spanId) {
		this.traceId = traceId;
		this.parentSpanId = parentSpanId;
		this.spanId = spanId;
		this.sampled = null;
	}
	
	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getParentSpanId() {
		return parentSpanId;
	}

	public void setParentSpanId(String parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public String getSpanId() {
		return spanId;
	}

	public void setSpanId(String spanId) {
		this.spanId = spanId;
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
