package org.mule.modules.zipkinlogger.automation.functional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.annotations.Config;
import org.mule.modules.zipkinlogger.ZipkinLoggerConnector;
import org.mule.modules.zipkinlogger.config.ZipkinConsoleConnectorConfig;
import org.mule.modules.zipkinlogger.model.LoggerData;
import org.mule.modules.zipkinlogger.model.LoggerTag;
import org.mule.modules.zipkinlogger.model.TraceData;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import brave.Span.Kind;

public class CreateAndStartNewSpanTests extends AbstractTestCase<ZipkinLoggerConnector> {

	@Config
	ZipkinConsoleConnectorConfig config;

	public CreateAndStartNewSpanTests() {
		super(ZipkinLoggerConnector.class);
	}

	@Before
	public void setup() {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testStandaloneSpan() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		TraceData trace = getConnector().createAndStartSpan(null, "standalone_id", tags, Kind.SERVER, "myspan", "test");
		String spanId = trace.getSpanId();

		try {
			getConnector().finishSpan('"' + spanId + '"');
		} catch (RuntimeException e) {
			fail("Exception thrown while finishing the span. spanId search didn't work");
		}

		// No parentId
		assertNull(trace.getParentSpanId());

		// If not found, returns null
		try {
			getConnector().finishSpan("9999L");
			fail("Exception not thrown");
		} catch (RuntimeException e) {

		}

	}

	@Test
	public void testSpanWithParent() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		tags.setTraceData(new TraceData("be3c95060bc041d5", "f396f0aa5492fbe1", "2b29459eb5dfc892", null, null));

		TraceData trace = getConnector().createAndStartSpan(null, "join_id", tags, Kind.SERVER, "myspan", "test");
		String spanId = trace.getSpanId();

		try {
			getConnector().finishSpan('"' + spanId + '"');
		} catch (RuntimeException e) {
			fail("Exception thrown");
		}

		// There is a parentId
		assertNotNull(trace.getParentSpanId());

		// If not found, returns null
		try {
			getConnector().finishSpan("9999L");
			fail("Exception not thrown");
		} catch (RuntimeException e) {

		}
	}

}