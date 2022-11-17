package org.owizen.tntaggregation.rest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.owizen.tntaggregation.model.Aggregation;
import org.owizen.tntaggregation.service.PricingService;
import org.owizen.tntaggregation.service.ShipmentsService;
import org.owizen.tntaggregation.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The AggregationRestController responsible for returning aggregated data of single request from the remotes APIs.
 */
@RestController
public class AggregationRestController {


	private PricingService pricingService;
	private TrackService trackService;
	private ShipmentsService shipmentsService;

	private AllOfCompletableFutureProvider provider = new AllOfCompletableFutureProvider();


	/**
	 * Constructor.
	 *
	 * @param pricingService   The Service responsible for returning pricing data of the remote API
	 * @param trackService     The Service responsible for returning tract data of the remote API
	 * @param shipmentsService The Service responsible for returning shipments data of the remote API
	 */
	public AggregationRestController(PricingService pricingService, TrackService trackService, ShipmentsService shipmentsService) {
		super();

		this.pricingService   = pricingService;
		this.trackService     = trackService;
		this.shipmentsService = shipmentsService;
	}

	/**
	 * Returns an Aggregation matching the HTTP request as json.
	 *
	 * @param pricing   The requested pricing
	 * @param track     The requested track
	 * @param shipments The requested shipments
	 *
	 * @return The Aggregation matching the request parameters
	 */
	@GetMapping
	public Aggregation get(
			@RequestParam(name = "pricing", required = false) List<String> pricing,
			@RequestParam(name = "track", required = false) List<Long> track,
			@RequestParam(name = "shipments", required = false) List<Long> shipments) {
		CompletableFuture<Map<String, Double>>     p;
		CompletableFuture<Map<Long, String>>       t;
		CompletableFuture<Map<Long, List<String>>> s;

		try {
			p = pricingService.get(pricing);
			t = trackService.get(track);
			s = shipmentsService.get(shipments);

			CompletableFuture<Void> lock = provider.allOf(p, s, t);

			lock.join();

			return new Aggregation(p.get(), t.get(), s.get());
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
		}
	}

}
