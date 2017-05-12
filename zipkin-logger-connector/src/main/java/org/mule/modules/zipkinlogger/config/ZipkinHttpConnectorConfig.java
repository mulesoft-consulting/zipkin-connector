package org.mule.modules.zipkinlogger.config;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;

@Configuration(friendlyName = "ZipkinHTTPConfiguration")
public class ZipkinHttpConnectorConfig extends AbstractConfig {

	/**
	 * Zipkin URL
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

	/**
	 * Get greeting message
	 */
	public String getZipkinUrl() {
		return this.zipkinUrl;
	}

	/**
	 * Set service name
	 *
	 * @param name
	 *            Service name to report to Zipkin
	 */
	public void setServiceName(String name) {
		this.serviceName = name;
	}

	/**
	 * Get reply
	 */
	public String getServiceName() {
		return this.serviceName;
	}

}