package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;

import brave.Span;
import brave.Span.Kind;

public class JoinExternalSpanAsyncTestCases extends AbstractTestCase<ZipkinLoggerConnector> {

	public JoinExternalSpanAsyncTestCases() {
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
		String flowVariableToSetWithId = "test";
		String traceName = "mytrace";

		getConnector().createNewTrace(null, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, traceName);

		Span span1 = getConnector().getSpansInFlight().values().iterator().next();

		String spanId1 = Long.toHexString(span1.context().spanId());

		brave.Span.Kind ServerOrClientSpanType1 = Kind.CLIENT;
		java.lang.String spanName1 = "another";
		java.lang.String traceId = Long.toHexString(span1.context().traceId());
		java.lang.String sampled = span1.context().sampled() ? "1" : "0";
		java.lang.String flags = span1.context().debug() ? "1" : "0";
		getConnector().joinExternalSpanAsync(logMessage, additionalTags, ServerOrClientSpanType1, spanName1, spanId1,
				spanId1, traceId, sampled, flags);

		getConnector().finishSpan(spanId1);
	}

}