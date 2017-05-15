package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.model.SpanData;

import brave.Span.Kind;

public class CreateNewTraceTestCases extends AbstractTestCase<ZipkinLoggerConnector> {

	public CreateNewTraceTestCases() {
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

		Map<String, String> additionalTags = null;

		Kind ServerOrClientSpanType = Kind.SERVER;
		String spanName = "span1";
		String traceName = "mytrace";

		SpanData spanData = getConnector().createNewTrace(logMessage, additionalTags, ServerOrClientSpanType, spanName,
				traceName);

		String spanId = spanData.getSpanId();

		getConnector().finishSpan(spanId);

		assertEquals(spanId, spanData.getSpanId());

	}

}