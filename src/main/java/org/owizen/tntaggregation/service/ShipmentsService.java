package org.owizen.tntaggregation.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.owizen.tntaggregation.config.ApiConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class ShipmentsService extends AbstractMapAPIService<Long, List<String>>{


	public ShipmentsService(ApiConfig config, @Qualifier("asyncExecutor") Executor executor) {
		super(config.getApiUrl(), config.getShipmentsPath(), LONG_STRING_LIST_MAP_REF, executor);
	}

	@Override
	@Async(value = "asyncExecutor")
	public CompletableFuture<Map<Long, List<String>>> get(List<Long> items) throws InterruptedException {
		return super.get(items);
	}

}
