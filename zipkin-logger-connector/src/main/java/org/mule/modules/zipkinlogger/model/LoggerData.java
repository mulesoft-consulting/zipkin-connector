
package org.mule.modules.zipkinlogger.model;

import java.util.ArrayList;
import java.util.List;

public class LoggerData {

	private TraceData traceData;
	private List<LoggerTag> loggerTags = new ArrayList<LoggerTag>();

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public LoggerData() {
	}

	/**
	 * 
	 * @param loggerTags
	 * @param traceData
	 */
	public LoggerData(TraceData traceData, List<LoggerTag> loggerTags) {
		super();
		this.traceData = traceData;
		this.loggerTags = loggerTags;
	}

	public TraceData getTraceData() {
		return traceData;
	}

	public void setTraceData(TraceData traceData) {
		this.traceData = traceData;
	}

	public List<LoggerTag> getLoggerTags() {
		return loggerTags;
	}

	public void setLoggerTags(List<LoggerTag> loggerTags) {
		this.loggerTags = loggerTags;
	}

}
