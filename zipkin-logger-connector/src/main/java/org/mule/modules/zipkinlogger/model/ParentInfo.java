package org.mule.modules.zipkinlogger.model;

public class ParentInfo {

	public ParentInfo(String traceId, String parentSpanId, String spanId, String sampled) {
		this.traceId = traceId;
		this.parentSpanId = parentSpanId;
		this.spanId = spanId;
		this.sampled = sampled;
	}

	private String traceId;
	private String parentSpanId;
	private String spanId;
	private String sampled;

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

}
