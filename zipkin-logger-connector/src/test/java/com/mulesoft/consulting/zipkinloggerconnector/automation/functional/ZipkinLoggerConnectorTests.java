package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.annotations.Config;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.config.ZipkinConsoleConnectorConfig;

import brave.Span.Kind;

public class ZipkinLoggerConnectorTests extends AbstractTestCase<ZipkinLoggerConnector> {

	@Config
	ZipkinConsoleConnectorConfig config;

	public ZipkinLoggerConnectorTests() {
		super(ZipkinLoggerConnector.class);
	}

	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testJoinExternalSpan() {
		Map<String, String> tagMap = new HashMap<String, String>();
		tagMap.put("test1", "12345a");

		String spanId = getConnector().joinExternalSpan(null, "logtest 123", tagMap, Kind.SERVER, "myspan1", "test",
				"be3c95060bc041d5", "f396f0aa5492fbe1", "2b29459eb5dfc892", null, null);

		getConnector().finishSpan(spanId);
	}

	@Test
	public void testJoinSpanException() {
		Map<String, String> tagMap = new HashMap<String, String>();
		tagMap.put("test1", "12345a");

		try {
			getConnector().joinSpan(null, "logtest 123", tagMap, Kind.SERVER, "myspan1", "test", "test123");
			fail("Should throw an excetion");
		} catch (Throwable e) {

		}
	}
}
