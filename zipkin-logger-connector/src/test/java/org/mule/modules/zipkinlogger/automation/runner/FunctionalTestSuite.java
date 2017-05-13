package org.mule.modules.zipkinlogger.automation.runner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mule.modules.zipkinlogger.automation.functional.CreateAndStartNewSpanTests;
import org.mule.tools.devkit.ctf.mockup.ConnectorTestContext;

import com.mulesoft.consulting.zipkinloggerconnector.ZipkinLoggerConnector;

@RunWith(Suite.class)
@SuiteClasses({
	CreateAndStartNewSpanTests.class
})

public class FunctionalTestSuite {
	
	@BeforeClass
	public static void initialiseSuite(){
		ConnectorTestContext.initialize(ZipkinLoggerConnector.class);
	}
	
	@AfterClass
    public static void shutdownSuite() {
    	ConnectorTestContext.shutDown();
    }
	
}