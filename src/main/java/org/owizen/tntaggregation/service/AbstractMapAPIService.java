package org.owizen.tntaggregation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.owizen.tntaggregation.service.fetch.FetchRunnableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;


/**
 * The AbstractMapAPIService class.
 *
 * This is a basic class for requesting data from a remote API returning a simple Key/Value json.
 *
 * @param <K> The type of the keys to be requested and returned by the remote API
 * @param <V> The type of the values to returned by the remote API
 */
public abstract class AbstractMapAPIService<K, V> {


	private static final int FETCH_MAX_TIME_ELAPSED = 5000;

	private static final int MAX_QUEUE_SIZE = 5;

	private static final String API_QUERY = "q";

	public static final ParameterizedTypeReference<Map<String, Double>> STRING_DOUBLE_MAP_REF = new ParameterizedTypeReference<>() {};
	public static final ParameterizedTypeReference<Map<Long, String>> LONG_STRING_MAP_REF = new ParameterizedTypeReference<>() {};
	public static final ParameterizedTypeReference<Map<Long, List<String>>> LONG_STRING_LIST_MAP_REF = new ParameterizedTypeReference<>() {};

	/**
	 * The ParameterizedTypeReference holding the exact generic type of the Map fetched by this service.
	 */
	private ParameterizedTypeReference<Map<K, V>> mapTypeRef;
	/**
	 * The WebClient used to fetch data from the Remote API
	 */
	private WebClient client;
	/**
	 * The path to the data of the remote API.
	 */
	private String path;
	/**
	 * The Executor to execute asynchonous task
	 */
	private Executor executor;
	/**
	 * The queue to store the requested key to the remote API for further processing
	 */
	private ArrayBlockingQueue<K> queue = new ArrayBlockingQueue<K>(10);
	/**
	 * The actual requests to be fulfilled as response of the rest controller calls.
	 */
	private Map<CompletableFuture<Map<K,V>>, List<K>> pending = new ConcurrentHashMap<>();
	/**
	 * The workaround too instantiate FetchRunnables.
	 */
	private FetchRunnableProvider provider = new FetchRunnableProvider();

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String serviceName = getClass().getSimpleName();
	/**
	 * Flag to prevent request to the API to occur simultaneously
	 */
	private boolean fetching = false;
	/**
	 * Last insertion into the queue timestamp
	 */
	private long lastInsert = System.currentTimeMillis();


	/**
	 * Constructor.
	 *
	 * @param apiUrl     The remote API url
	 * @param path       The path to the data of the remote url
	 * @param mapTypeRef The ParameterizedTypeReference to wrap the parameterized type of the Maps from the remote API
	 * @param executor
	 */
	public AbstractMapAPIService(String apiUrl, String path, ParameterizedTypeReference<Map<K, V>> mapTypeRef, Executor executor) {
		super();

		this.path       = path;
		this.client     = WebClient.create(apiUrl);
		this.mapTypeRef = mapTypeRef;
		this.executor   = executor;
	}

	/**
	 * Update the insertion to queue timestamp
	 */
	private void updateLastInsert() {
		lastInsert = System.currentTimeMillis();
	}

	/**
	 * The timer to schedule data fetching when the pending requests are too long in the queue although not in sufficient quantity
	 */
	@Scheduled(fixedRate = 2000)
	public void timer() {
		if (fetching) {
			logger.debug("{} Ongoing fetching detected, aborting scheduling", serviceName);
			return;
		}

		if (pending.isEmpty()) {
			logger.debug("{} No pending fetching to process, aborting scheduling", serviceName);
			return;
		}

		if (! insertEOLlapsed()) {
			logger.debug("{} Fetch max time not reached, aborting scheduling", serviceName);
			return;
		}

		logger.info("{} perform schduled fetching", serviceName);

		fetch(queue.size());
	}

	/**
	 * Whether the pending request timeout
	 *
	 * @return
	 */
	private boolean insertEOLlapsed() {
		return System.currentTimeMillis() - lastInsert > FETCH_MAX_TIME_ELAPSED;
	}

	/**
	 * Returns a Mono of the request to the remote API.
	 *
	 * @param keys The keys to request value for from the remote API.
	 *
	 * @return
	 */
	public Mono<Map<K, V>> fetch(List<K> keys) {
		return client.get()
				.uri(uriBuilder -> uriBuilder
					.path(path)
				    .queryParam(API_QUERY, toQuery(keys))
				    .build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(mapTypeRef);
	}

	/**
	 * Adds the given future to the pending one with its associated keys
	 *
	 * @param keys
	 * @param future
	 */
	private void submit(List<K> keys, CompletableFuture<Map<K,V>> future) {
		if (keys == null) {
			future.complete(new HashMap<>());
			return;
		}

		int oldSize = pending.size();

		pending.put(future, keys);

		logger.info("Future submitted, went from {} to {} pending future in {}", oldSize, pending.size(), serviceName);

		queue.addAll(keys);

		updateLastInsert();

		int size = queue.size();

		if (size < MAX_QUEUE_SIZE) {
			return;
		}

		fetch(size);
	}

	/**
	 * Fetch data from the remote API by pulling the given amount of keys from the queue.
	 *
	 * @param size
	 */
	private void fetch(int size) {
		if (fetching) {
			return;
		}

		fetching = true;

		List<K> candidates = new ArrayList<>();

		queue.drainTo(candidates, size);

		executor.execute(provider.getFetchRunnable(this, candidates));
	}

	/**
	 * Update the pending requests/CompletableFuture and complete them when possible
	 *
	 * @param candidates The keys that were requested from the remote API
	 * @param bulk       The return mapped values by keys of the remote API
	 */
	public void update(List<K> candidates, Map<K, V> bulk) {
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

			logger.info("Future completed, {} left from {} in {} at {}", pending.size(), oldSize, serviceName, System.currentTimeMillis());

			future.complete(complete);
		}

		fetching = false;
	}

	/**
	 * Flush the pending requests and complete the pending CompletableFuture
	 * @param msg
	 */
	public void invalidate(String msg) {
		for (CompletableFuture<Map<K,V>> future : pending.keySet()) {
			logger.info("Error occured when fetching data with reason: {}", msg);
			future.completeExceptionally(new RuntimeException(msg));
		}

		pending.clear();

		fetching = false;
	}

	/**
	 * Suscribe the given request as keys to the remote API by returninf a pending CompletableFuture.
	 *
	 * @param items The keys of the data to request from the remote API
	 *
	 * @return
	 *
	 * @throws InterruptedException
	 */
	protected CompletableFuture<Map<K,V>> get(List<K> items) throws InterruptedException {
		logger.info("New request submited to fetch from API with items {} using {}", items, serviceName);

		CompletableFuture<Map<K,V>> future = new CompletableFuture<>();

		submit(items, future);

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			throw e;
		}

		return future;
	}

	public Map<CompletableFuture<Map<K, V>>, List<K>> getPending() {
		return pending;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setFetching(boolean fetching) {
		this.fetching = fetching;
	}

	protected String toQuery(List<K> items) {
		return items.stream().map(Object::toString).collect(Collectors.joining(","));

	}

}
