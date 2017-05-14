package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.*;
import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

public class FinishSpanTestCases extends AbstractTestCase<ZipkinLoggerConnector> {

	public FinishSpanTestCases() {
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
		java.lang.String expressionToGetSpanId = null;
		assertEquals(getConnector().finishSpan(expressionToGetSpanId), expected);
	}

}