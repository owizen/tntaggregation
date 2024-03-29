package org.owizen.tntaggregation.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.owizen.tntaggregation.config.ApiConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 *The service responsible for fetching track from the remote API.
 */
@Service
public class TrackService extends AbstractMapAPIService<Long, String> {


	public TrackService(ApiConfig config) {
		super(config.getApiUrl(), config.getTrackPath(), LONG_STRING_MAP_REF);
	}


	@Override
	@Async(value = "asyncExecutor")
	public CompletableFuture<Map<Long, String>> get(List<Long> items) throws InterruptedException {
		return super.get(items);
	}

}
