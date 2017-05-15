package com.mulesoft.consulting.zipkinloggerconnector.config;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;

/*
 * Zipkin connector configuration that logs spans to console. Useful for testing purposes.
 * 
 * 
 * @author michaelhyatt
 * 
 */
@Configuration(friendlyName = "Console Configuration", configElementName = "console-config")
public class ZipkinConsoleConnectorConfig extends AbstractConfig {

	/**
	 * Service name to be sent to Zipkin
	 */
	@Configurable
	@Default("my-service")
	private String serviceName;

	public String getServiceName() {
		return serviceName;
	}

	/*
	 * @param serviceName Service name to apply to logged spans
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}