package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.*;
import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

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
		java.lang.String expected = null;
		org.mule.api.MuleEvent muleEvent = null;
		java.lang.String logMessage = null;
		java.util.Map<java.lang.String, java.lang.String> additionalTags = null;
		brave.Span.Kind ServerOrClientSpanType = null;
		java.lang.String spanName = null;
		java.lang.String flowVariableToSetWithId = null;
		java.lang.String parentSpanId = null;
		assertEquals(getConnector().joinSpan(muleEvent, logMessage, additionalTags, ServerOrClientSpanType, spanName,
				flowVariableToSetWithId, parentSpanId), expected);
	}

}