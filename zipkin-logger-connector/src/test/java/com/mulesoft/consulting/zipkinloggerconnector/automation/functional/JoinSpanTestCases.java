package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.model.SpanData;

import brave.Span.Kind;

public class JoinSpanTestCases extends AbstractTestCase<ZipkinLoggerConnector> {

	public JoinSpanTestCases() {
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

		SpanData spanData1 = getConnector().createNewTrace(logMessage, additionalTags, ServerOrClientSpanType, spanName,
				traceName);

		String spanId1 = spanData1.getSpanId();

		brave.Span.Kind ServerOrClientSpanType1 = Kind.CLIENT;
		java.lang.String spanName1 = "myspan";

		SpanData spanData2 = getConnector().joinSpan(logMessage, additionalTags, ServerOrClientSpanType1, spanName1,
				spanId1);

		String spanId2 = spanData2.getSpanId();

		getConnector().finishSpan(spanId2);

		getConnector().finishSpan(spanId1);

		assertEquals(spanData1.getSpanId(), spanData2.getParentSpanId());
	}

}