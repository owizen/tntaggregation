package org.owizen.tntaggregation.service.fetch;

import java.util.List;
import java.util.Map;

import org.owizen.tntaggregation.service.AbstractMapAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

/**
 * The Runnable responsible for asynchronously fetching data from the remote API.
 *
 * @param <K> The type of the key of the remote API data
 * @param <V> The type of the value of the remote API data
 */
public class FetchRunnable<K, V> implements Runnable {

	private List<K> candidates;
	private AbstractMapAPIService<K, V> service;
	private Logger logger;


	public FetchRunnable(AbstractMapAPIService<K, V> service, List<K> candidates) {
		super();

		this.service    = service;
		this.candidates = candidates;
		this.logger     = LoggerFactory.getLogger(service.getClass());

	}

	@Override
	public void run() {
		logger.info("Fetch Data from {} API", service.getServiceName());

		Mono<Map<K, V>> bulkMono = service.fetch(candidates);

		bulkMono.subscribe(
			// On success
			bulk -> service.update(candidates, bulk),
			// On error
			e -> service.invalidate(e.getMessage())
		);
	}



}
