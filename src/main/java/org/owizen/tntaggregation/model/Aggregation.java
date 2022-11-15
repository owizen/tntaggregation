package org.owizen.tntaggregation.model;

import java.util.List;
import java.util.Map;

public class Aggregation {


	private Map<String, Double> pricing;

	private Map<Long, String> track;

	private Map<Long, List<String>> shipments;



	public Aggregation(Map<String, Double> pricing, Map<Long, String> track, Map<Long, List<String>> shipments) {
		super();

		this.pricing   = pricing;
		this.track     = track;
		this.shipments = shipments;
	}


	public Map<String, Double> getPricing() {
		return pricing;
	}

	public Map<Long, String> getTrack() {
		return track;
	}

	public Map<Long, List<String>> getShipments() {
		return shipments;
	}

}
