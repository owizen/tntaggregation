package org.owizen.tntaggregation.service;

import java.util.List;

import org.owizen.tntaggregation.config.ApiConfig;
import org.springframework.stereotype.Service;


@Service
public class TrackService extends AbstractMapAPIService<Long, String> {


	public TrackService(ApiConfig config) {
		super(config.getApiUrl(), config.getTrackPath(), LONG_STRING_MAP_REF);
	}

	@Override
	protected String toQuery(List<Long> items) {
		return toQueryFromLong(items);
	}

}
