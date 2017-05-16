package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.model.SpanData;

import brave.Span.Kind;

public class JoinExternalSpanTestCases extends AbstractTestCase<ZipkinLoggerConnector> {

	public JoinExternalSpanTestCases() {
		super(ZipkinLoggerConnector.class);
	}

	@Before
	public void setup() {
		// TODO
	}

	@After
	public void tearDown() {
		// TODO
	}

	@Test
	public void verify() {

		String logMessage = "test log message";

		Map<String, String> additionalTags = new HashMap<String, String>();
		additionalTags.put("teet", "terer");

		Kind ServerOrClientSpanType = Kind.SERVER;
		String spanName = "span1";
		String traceName = "mytrace";

		SpanData spanData = getConnector().createNewTrace(logMessage, additionalTags, ServerOrClientSpanType, spanName,
				traceName);

		String spanId1 = spanData.getSpanId();

		brave.Span.Kind ServerOrClientSpanType1 = Kind.CLIENT;
		java.lang.String spanName1 = "myspan";

		java.lang.String traceId = spanData.getTraceId();
		java.lang.String sampled = spanData.getSampled();
		java.lang.String flags = spanData.getDebug();

		SpanData spanData1 = getConnector().joinExternalSpan(logMessage, additionalTags, ServerOrClientSpanType1,
				spanName1, spanId1, spanId1, traceId, sampled, flags);

		String spanId2 = spanData1.getSpanId();

		getConnector().finishSpan(spanId2, "123", additionalTags, null);

		getConnector().finishSpan(spanId1, "123", additionalTags, null);
	}

}