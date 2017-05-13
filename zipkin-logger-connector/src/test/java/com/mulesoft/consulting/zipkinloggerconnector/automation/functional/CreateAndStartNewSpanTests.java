package com.mulesoft.consulting.zipkinloggerconnector.automation.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.annotations.Config;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.config.ZipkinConsoleConnectorConfig;
import com.mulesoft.consulting.zipkinloggerconnector.model.LoggerData;
import com.mulesoft.consulting.zipkinloggerconnector.model.LoggerTag;
import com.mulesoft.consulting.zipkinloggerconnector.model.TraceData;

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
	public void test3NestedSpans() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		TraceData trace1 = getConnector().createAndStartSpan(null, "standalone_id", tags, Kind.CLIENT, "myspan1", "test");

		LoggerData tags2 = new LoggerData();
		tags2.setLoggerTags(tags.getLoggerTags());
		tags2.setTraceData(trace1);
		
		TraceData trace2 = getConnector().createAndStartSpan(null, "join_id", tags2, Kind.SERVER, "myspan2", "test");
		
		LoggerData tags3 = new LoggerData();
		tags3.setLoggerTags(tags.getLoggerTags());
		tags3.setTraceData(trace2);
		
		TraceData trace3 = getConnector().createAndStartSpan(null, "join_id", tags3, Kind.CLIENT, "myspan3", "test");

		try {
			getConnector().finishSpan(trace3.getSpanId());
			getConnector().finishSpan(trace2.getSpanId());
			getConnector().finishSpan(trace1.getSpanId());
		} catch (Throwable e) {
			fail("finishSpans reported exception");
		}
		
		assertEquals(trace3.getParentSpanId(),trace2.getSpanId());
		assertEquals(trace2.getParentSpanId(),trace1.getSpanId());
		assertEquals(trace1.getTraceId(), trace1.getSpanId());
		
	}

	/*
	 * Creates new span
	 */
	@Test
	public void testStandaloneSpan() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		TraceData trace = getConnector().createAndStartSpan(null, "standalone_id", tags, Kind.SERVER, "myspan", "test");
		String spanId = trace.getSpanId();

		try {
			getConnector().finishSpan(spanId);
		} catch (RuntimeException e) {
			fail("Exception thrown while finishing the span. spanId search didn't work");
		}

		// No parentId
		assertEquals(trace.getParentSpanId(), trace.getSpanId());

		// If not found, returns null
		try {
			getConnector().finishSpan("9999L");
			fail("Exception not thrown");
		} catch (RuntimeException e) {

		}

	}

	/*
	 * Joins the parent trace
	 */
	@Test
	public void testSpanWithParentSpanIdAndSpanId() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		tags.setTraceData(new TraceData("be3c95060bc041d5", "f396f0aa5492fbe1", "2b29459eb5dfc892", null, null));

		TraceData trace = getConnector().createAndStartSpan(null, "join_id", tags, Kind.SERVER, "myspan", "test");
		String spanId = trace.getSpanId();

		try {
			getConnector().finishSpan(spanId);
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

	/*
	 * If no spanId is provided, starts its own trace
	 */
	@Test
	public void testSpanWithParentSpanIdAndNoSpanId() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		tags.setTraceData(new TraceData("be3c95060bc041d5", null, "2b29459eb5dfc892", null, null));

		TraceData trace = getConnector().createAndStartSpan(null, "join_id", tags, Kind.SERVER, "myspan", "test");
		String spanId = trace.getSpanId();

		try {
			getConnector().finishSpan(spanId);
		} catch (RuntimeException e) {
			fail("Exception thrown");
		}

		// There is a parentId, but it is equal to spanId and it is weird.
		// Illegal state.
		assertEquals(trace.getParentSpanId(), trace.getSpanId());

	}

}