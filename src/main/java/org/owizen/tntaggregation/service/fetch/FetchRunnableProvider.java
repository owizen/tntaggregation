package org.owizen.tntaggregation.service.fetch;

import java.util.List;

import org.owizen.tntaggregation.service.AbstractMapAPIService;

public class FetchRunnableProvider {

	public <K, V> FetchRunnable<K, V> getFetchRunnable(AbstractMapAPIService<K, V> service, List<K> candidates) {
		return new FetchRunnable<K, V>(service, candidates);
	}

}
