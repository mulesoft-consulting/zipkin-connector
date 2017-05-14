package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.*;
import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

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
		java.lang.String expected = null;
		org.mule.api.MuleEvent muleEvent = null;
		java.lang.String logMessage = null;
		java.util.Map<java.lang.String, java.lang.String> additionalTags = null;
		brave.Span.Kind ServerOrClientSpanType = null;
		java.lang.String spanName = null;
		java.lang.String flowVariableToSetWithId = null;
		java.lang.String traceName = null;
		assertEquals(getConnector().createNewTrace(muleEvent, logMessage, additionalTags, ServerOrClientSpanType,
				spanName, flowVariableToSetWithId, traceName), expected);
	}

}