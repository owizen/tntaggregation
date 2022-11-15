package org.owizen.tntaggregation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.owizen.tntaggregation.service.fetch.FetchRunnableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


public abstract class AbstractMapAPIService<K, V> {


	private static final int MAX_QUEUE_SIZE = 5;

	private static final String API_QUERY = "q";

	public static final ParameterizedTypeReference<Map<String, Double>> STRING_DOUBLE_MAP_REF = new ParameterizedTypeReference<>() {};
	public static final ParameterizedTypeReference<Map<Long, String>> LONG_STRING_MAP_REF = new ParameterizedTypeReference<>() {};
	public static final ParameterizedTypeReference<Map<Long, List<String>>> LONG_STRING_LIST_MAP_REF = new ParameterizedTypeReference<>() {};


	private ParameterizedTypeReference<Map<K, V>> mapTypeRef;
	private WebClient client;
	private String path;
	private Executor executor;

	private ArrayBlockingQueue<K> queue = new ArrayBlockingQueue<K>(10);
	private Map<CompletableFuture<Map<K,V>>, List<K>> pending = new ConcurrentHashMap<>();
	private FetchRunnableProvider provider = new FetchRunnableProvider();

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String serviceName = getClass().getSimpleName();


	public AbstractMapAPIService(String apiUrl, String path, ParameterizedTypeReference<Map<K, V>> mapTypeRef, Executor executor) {
		super();

		this.path       = path;
		this.client     = WebClient.create(apiUrl);
		this.mapTypeRef = mapTypeRef;
		this.executor   = executor;
	}

	public Map<K,V> fetch(List<K> keys) {
		return client.get()
				.uri(uriBuilder -> uriBuilder
					.path(path)
				    .queryParam(API_QUERY, toQuery(keys))
				    .build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(mapTypeRef).block();
	}

	private void submit(List<K> keys, CompletableFuture<Map<K,V>> future) {
		if (keys == null) {
			future.complete(new HashMap<>());
		}
		int oldSize = pending.size();

		pending.put(future, keys);

		logger.info("Future submitted, went from {} to {} pending future in {}", oldSize, pending.size(), serviceName);

		queue.addAll(keys);

		fetch();
	}

	private void fetch() {
		int size = queue.size();

		if (size < MAX_QUEUE_SIZE) {
			return;
		}

		List<K> candidates = new ArrayList<>();

		queue.drainTo(candidates, size);

		executor.execute(provider.getFetchRunnable(this, candidates));
	}

	public void invalidate(String msg) {
		for (CompletableFuture<Map<K,V>> future : pending.keySet()) {
			logger.info("Error occured when fetching data with reason: {}", msg);
			future.completeExceptionally(new RuntimeException(msg));
		}

		pending.clear();
	}

	protected CompletableFuture<Map<K,V>> get(List<K> items) throws InterruptedException {
		logger.info("New request submited to fetch from API with items {} using {}", items, serviceName);

		CompletableFuture<Map<K,V>> future = new CompletableFuture<>();

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			throw e;
		}

		submit(items, future);

		return future;
	}

	protected String toQueryFromLong(List<Long> items) {
		return items.stream().map(Object::toString).collect(Collectors.joining(","));
	}

	protected String toQueryFromString(List<String> pricing) {
		return String.join(",", pricing.toArray(new String[pricing.size()]));
	}

	public Map<CompletableFuture<Map<K, V>>, List<K>> getPending() {
		return pending;
	}

	public String getServiceName() {
		return serviceName;
	}

	protected abstract String toQuery(List<K> items);

}
