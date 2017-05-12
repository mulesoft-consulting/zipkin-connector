package org.mule.modules.zipkinlogger.automation.functional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
	
	@Config ZipkinConsoleConnectorConfig config;
	
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

		brave.Span span = getConnector().createAndStartSpan(null, "standalone_id", tags, Kind.SERVER, "myspan", "test");
		Long spanId = span.context().spanId();
		brave.Span result = getConnector().finishSpan(spanId.toString());

		// No parentId
		assertNull(span.context().parentId());

		// Found in subsequent call and is the same as the original one
		assertNotNull(result);
		assertSame(span, result);

		// If not found, returns null
		brave.Span resultNull = getConnector().finishSpan("9999L");
		assertNull(resultNull);

	}

	@Test
	public void testSpanWithParent() {

		LoggerData tags = new LoggerData();
		tags.getLoggerTags().add(new LoggerTag("test1", "value123"));
		tags.getLoggerTags().add(new LoggerTag("test2", "value789"));

		tags.setTraceData(new TraceData("be3c95060bc041d5", "f396f0aa5492fbe1", "2b29459eb5dfc892", null, null));

		brave.Span span = getConnector().createAndStartSpan(null, "join_id", tags, Kind.SERVER, "myspan", "test");
		Long spanId = span.context().spanId();
		brave.Span result = getConnector().finishSpan(spanId.toString());

		// There is a parentId
		assertNotNull(span.context().parentId());

		// Found in subsequent call and is the same as the original one
		assertNotNull(result);
		assertSame(span, result);

		// If not found, returns null
		brave.Span resultNull = getConnector().finishSpan("9999L");
		assertNull(resultNull);

	}

}