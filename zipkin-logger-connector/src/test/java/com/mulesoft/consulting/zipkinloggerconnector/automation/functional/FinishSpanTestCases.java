package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

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
	public void verify() {

		getConnector().finishSpan("1231231");
	}

}