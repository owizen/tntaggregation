package org.owizen.tntaggregation.service;

import java.util.List;

import org.owizen.tntaggregation.config.ApiConfig;
import org.springframework.stereotype.Service;


@Service
public class PricingService extends AbstractMapAPIService<String, Double> {


	public PricingService(ApiConfig config) {
		super(config.getApiUrl(), config.getPricingPath(), STRING_DOUBLE_MAP_REF);
	}

	@Override
	protected String toQuery(List<String> items) {
		return toQueryFromString(items);
	}

}
