package org.owizen.tntaggregation.service;

import java.util.List;

import org.owizen.tntaggregation.config.ApiConfig;
import org.springframework.stereotype.Service;


@Service
public class ShipmentsService extends AbstractMapAPIService<Long, List<String>>{


	public ShipmentsService(ApiConfig config) {
		super(config.getApiUrl(), config.getShipmentsPath(), LONG_STRING_LIST_MAP_REF);
	}

	@Override
	protected String toQuery(List<Long> items) {
		return toQueryFromLong(items);
	}

}
