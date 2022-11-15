package org.owizen.tntaggregation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "tnt.aggregation")
public class ApiConfig {


	private String apiUrl;
	private String pricingPath;
	private String trackPath;
	private String shipmentsPath;


	public String getApiUrl() {
		return apiUrl;
	}
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	public String getPricingPath() {
		return pricingPath;
	}
	public void setPricingPath(String pricingPath) {
		this.pricingPath = pricingPath;
	}
	public String getTrackPath() {
		return trackPath;
	}
	public void setTrackPath(String trackPath) {
		this.trackPath = trackPath;
	}
	public String getShipmentsPath() {
		return shipmentsPath;
	}
	public void setShipmentsPath(String shipmentsPath) {
		this.shipmentsPath = shipmentsPath;
	}

}
