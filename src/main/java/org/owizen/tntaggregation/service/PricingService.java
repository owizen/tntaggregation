package org.owizen.tntaggregation.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.owizen.tntaggregation.config.ApiConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 *The service responsible for fetching pricing from the remote API.
 */
@Service
public class PricingService extends AbstractMapAPIService<String, Double> {


	public PricingService(ApiConfig config, @Qualifier("asyncExecutor") Executor executor) {
		super(config.getApiUrl(), config.getPricingPath(), STRING_DOUBLE_MAP_REF, executor);
	}

	@Override
	@Async(value = "asyncExecutor")
	public CompletableFuture<Map<String, Double>> get(List<String> items) throws InterruptedException {
		return super.get(items);
	}

}
