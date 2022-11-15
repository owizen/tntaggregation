package org.owizen.tntaggregation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


public abstract class AbstractMapAPIService<K, V> {


	private static final String API_QUERY = "q";

	public static final ParameterizedTypeReference<Map<String, Double>> STRING_DOUBLE_MAP_REF = new ParameterizedTypeReference<>() {};
	public static final ParameterizedTypeReference<Map<Long, String>> LONG_STRING_MAP_REF = new ParameterizedTypeReference<>() {};
	public static final ParameterizedTypeReference<Map<Long, List<String>>> LONG_STRING_LIST_MAP_REF = new ParameterizedTypeReference<>() {};


	private ParameterizedTypeReference<Map<K, V>> mapTypeRef;
	private WebClient client;
	private String path;


	public AbstractMapAPIService(String apiUrl, String path, ParameterizedTypeReference<Map<K, V>> mapTypeRef) {
		super();

		this.path       = path;
		this.client     = WebClient.create(apiUrl);
		this.mapTypeRef = mapTypeRef;

	}

	public Map<K, V> fetch(List<K> items) {
		if (items == null) {
			return new HashMap<>();
		}

		return fetch(toQuery(items));
	}

	protected Map<K,V> fetch(String query) {
		return client.get()
				.uri(uriBuilder -> uriBuilder
					.path(path)
				    .queryParam(API_QUERY, query)
				    .build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(mapTypeRef).block();
	}

	protected String toQueryFromLong(List<Long> items) {
		return items.stream().map(Object::toString).collect(Collectors.joining(","));
	}

	protected String toQueryFromString(List<String> pricing) {
		return String.join(",", pricing.toArray(new String[pricing.size()]));
	}

	protected abstract String toQuery(List<K> items);

}
