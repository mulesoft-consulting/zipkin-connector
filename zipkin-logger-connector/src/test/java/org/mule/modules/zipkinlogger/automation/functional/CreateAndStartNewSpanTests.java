package org.mule.modules.zipkinlogger.automation.functional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.modules.zipkinlogger.ZipkinLoggerConnector;
import org.mule.modules.zipkinlogger.model.LoggerTags;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import brave.Span.Kind;
import brave.Tracer;
import brave.Tracing;
import zipkin.Span;
import zipkin.reporter.Reporter;

public class CreateAndStartNewSpanTests extends AbstractTestCase<ZipkinLoggerConnector> {

	private Tracing tracing;

	@Inject
	MuleContext muleContext;

	public CreateAndStartNewSpanTests() {
		super(ZipkinLoggerConnector.class);
	}

	@Before
	public void setup() {
		Reporter<Span> reporter = Reporter.CONSOLE;

		tracing = Tracing.newBuilder().localServiceName("my-service").reporter(reporter).build();

		Tracer tracer = tracing.tracer();

		getConnector().setTracer(tracer);

	}

	@After
	public void tearDown() {
		tracing.close();
	}

	@Test
	public void testStandaloneSpan() {

		LoggerTags tags = new LoggerTags();
		tags.addTag("test1", "value123");
		tags.addTag("test2", "value789");

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

}