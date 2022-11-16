package org.owizen.tntaggregation.service.fetch;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.owizen.tntaggregation.service.AbstractMapAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchRunnable<K, V> implements Runnable {

	private Map<CompletableFuture<Map<K,V>>, List<K>> pending;
	private List<K> candidates;
	private AbstractMapAPIService<K, V> service;
	private Logger logger;

	public FetchRunnable(AbstractMapAPIService<K, V> service, List<K> candidates) {
		super();

		this.service    = service;
		this.candidates = candidates;
		this.pending    = service.getPending();
		this.logger     = LoggerFactory.getLogger(service.getClass());

	}

	@Override
	public void run() {
		logger.info("Fetch Data from {} API", service.getServiceName());

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			service.invalidate("Fetch Runnable interrupted unexpectedly");
			return;
		}

		Map<K, V> bulk;

		try {
			bulk = service.fetch(candidates);
		} catch (Exception e) {
			service.invalidate(e.getMessage());
			return;
		}

		update(candidates, bulk);

		service.setFetching(false);
	}

	private void update(List<K> candidates, Map<K, V> bulk) {
		for (Entry<CompletableFuture<Map<K, V>>, List<K>> entry : pending.entrySet()) {
			List<K> keys = entry.getValue();

			if (! candidates.containsAll(keys)) {
				continue;
			}

			CompletableFuture<Map<K, V>> future = entry.getKey();

			Map<K, V> complete = keys.stream()
					.collect(Collectors.toMap(Function.identity(), k -> bulk.get(k)));

			int oldSize = pending.size();

			pending.remove(future);

			logger.info("Future completed, {} left from {} in {} at {}", pending.size(), oldSize, service.getServiceName(), System.currentTimeMillis());

			future.complete(complete);
		}
	}

}
