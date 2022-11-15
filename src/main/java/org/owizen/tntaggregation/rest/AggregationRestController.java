package org.owizen.tntaggregation.rest;

import java.util.List;
import java.util.Map;

import org.owizen.tntaggregation.model.Aggregation;
import org.owizen.tntaggregation.service.PricingService;
import org.owizen.tntaggregation.service.ShipmentsService;
import org.owizen.tntaggregation.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AggregationRestController {


	private PricingService pricingService;
	private TrackService trackService;
	private ShipmentsService shipmentsService;



	public AggregationRestController(PricingService pricingService, TrackService trackService, ShipmentsService shipmentsService) {
		super();

		this.pricingService   = pricingService;
		this.trackService     = trackService;
		this.shipmentsService = shipmentsService;
	}

	@GetMapping
	public Aggregation get(
			@RequestParam(name = "pricing", required = false) List<String> pricing,
			@RequestParam(name = "track", required = false) List<Long> track,
			@RequestParam(name = "shipments", required = false) List<Long> shipments) {

		Map<String, Double>     p;
		Map<Long, String>       t;
		Map<Long, List<String>> s;

		try {
			p = pricingService.fetch(pricing);
			t = trackService.fetch(track);
			s = shipmentsService.fetch(shipments);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "TNT API not available (CODE 503)\n");
		}

		return new Aggregation(p, t, s);
	}

}
