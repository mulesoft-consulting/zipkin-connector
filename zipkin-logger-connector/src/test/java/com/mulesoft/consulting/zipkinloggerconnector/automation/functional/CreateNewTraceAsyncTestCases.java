package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.*;
import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

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
		java.lang.String expected = null;
		java.lang.String logMessage = null;
		java.util.Map<java.lang.String, java.lang.String> additionalTags = null;
		brave.Span.Kind ServerOrClientSpanType = null;
		java.lang.String spanName = null;
		java.lang.String traceName = null;
		assertEquals(getConnector().createNewTraceAsync(logMessage, additionalTags, ServerOrClientSpanType, spanName,
				traceName), expected);
	}

}