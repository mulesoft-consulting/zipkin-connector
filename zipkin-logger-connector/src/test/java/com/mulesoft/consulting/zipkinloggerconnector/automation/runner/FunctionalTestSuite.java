package com.mulesoft.consulting.zipkinloggerconnector.automation.runner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mule.api.annotations.Config;
import org.mule.tools.devkit.ctf.mockup.ConnectorTestContext;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.CreateNewTraceAsyncTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.CreateNewTraceTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.FinishSpanTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.JoinExternalSpanAsyncTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.JoinExternalSpanTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.JoinSpanAsyncTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.automation.functional.JoinSpanTestCases;
import com.mulesoft.consulting.zipkinloggerconnector.config.ZipkinConsoleConnectorConfig;

@RunWith(Suite.class)
@SuiteClasses({ CreateNewTraceTestCases.class, CreateNewTraceAsyncTestCases.class, FinishSpanTestCases.class,
		JoinExternalSpanTestCases.class, JoinExternalSpanAsyncTestCases.class, JoinSpanTestCases.class,
		JoinSpanAsyncTestCases.class })

public class FunctionalTestSuite {

	@Config
	ZipkinConsoleConnectorConfig config;

	@BeforeClass
	public static void initialiseSuite() {
		ConnectorTestContext.initialize(ZipkinLoggerConnector.class);
	}

	@AfterClass
	public static void shutdownSuite() {
		ConnectorTestContext.shutDown();
	}

}