package org.mule.modules.zipkinlogger.config;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;

@Configuration(friendlyName = "Zipkin Console Logging Configuration", configElementName = "console-config")
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

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}