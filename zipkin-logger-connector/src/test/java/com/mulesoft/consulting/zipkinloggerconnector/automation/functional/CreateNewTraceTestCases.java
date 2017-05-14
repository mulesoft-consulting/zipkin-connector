package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;

import brave.Span;
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
		String flowVariableToSetWithId = "test";
		String traceName = "mytrace";

		getConnector().createNewTrace(null, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, traceName);
		
		Span span = getConnector().getSpansInFlight().values().iterator().next();
		
		String spanId = Long.toHexString(span.context().spanId());
		
		getConnector().finishSpan(spanId);

	}

}