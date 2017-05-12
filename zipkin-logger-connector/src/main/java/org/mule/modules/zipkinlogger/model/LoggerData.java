
package org.mule.modules.zipkinlogger.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggerData {

    private TraceData traceData;
    private List<LoggerTag> loggerTags = new ArrayList<LoggerTag>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
