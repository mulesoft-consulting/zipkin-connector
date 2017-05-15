package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.model.SpanData;

import brave.Span.Kind;

public class CreateNewTraceAsyncTestCases extends AbstractTestCase<ZipkinLoggerConnector> {

	public CreateNewTraceAsyncTestCases() {
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

		Kind ServerOrClientSpanType = Kind.SERVER;
		String spanName = "span1";
		String traceName = "mytrace";

		SpanData span = getConnector().createNewTraceAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName, traceName);
		
		assertNotNull(span);
		
		assertEquals(span.getSpanId(), span.getParentSpanId());
	}

}