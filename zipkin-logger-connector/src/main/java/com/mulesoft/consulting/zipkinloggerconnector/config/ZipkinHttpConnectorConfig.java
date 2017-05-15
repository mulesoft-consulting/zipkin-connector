package com.mulesoft.consulting.zipkinloggerconnector.config;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;

/*
 * Connector configuration to log data into Zipkin server using HTTP
 * 
 * 
 * @author michaelhyatt
 * 
 */
@Configuration(friendlyName = "HTTP Configuration", configElementName = "http-config")
public class ZipkinHttpConnectorConfig extends AbstractConfig {

	/**
	 * Zipkin URL
	 * 
	 * Example:
	 * 
	 * <pre>
	 * http://127.0.0.1:9411/api/v1/spans
	 * </pre>
	 */
	@Configurable
	@Default("http://127.0.0.1:9411/api/v1/spans")
	private String zipkinUrl;

	/**
	 * Service name to be sent to Zipkin
	 */
	@Configurable
	@Default("my-service")
	private String serviceName;

	/**
	 * Set Zipkin service URL
	 *
	 * @param url
	 *            Zipkin service URL
	 */
	public void setZipkinUrl(String url) {
		this.zipkinUrl = url;
	}

	public String getZipkinUrl() {
		return this.zipkinUrl;
	}

	/**
	 * Set service name
	 *
	 * @param serviceName
	 *            Service name to apply to logged spans
	 * 
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return this.serviceName;
	}

}