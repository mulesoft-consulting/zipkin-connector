package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;

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

	@Test(expected = RuntimeException.class)
	public void verifyException() {

		getConnector().finishSpan("12", "test", null, false);
	}

	@Test
	public void verifyNoException() {

		assertNull(getConnector().finishSpan("12", "test", null, true));
	}

}