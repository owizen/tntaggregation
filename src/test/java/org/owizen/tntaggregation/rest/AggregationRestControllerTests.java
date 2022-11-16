package org.owizen.tntaggregation.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.owizen.tntaggregation.IntrospectionHelper;
import org.owizen.tntaggregation.model.Aggregation;
import org.owizen.tntaggregation.service.PricingService;
import org.owizen.tntaggregation.service.ShipmentsService;
import org.owizen.tntaggregation.service.TrackService;


@ExtendWith(MockitoExtension.class)
public class AggregationRestControllerTests implements IntrospectionHelper {


	AggregationRestController subject;

	@Mock PricingService pricingService;
	@Mock TrackService trackService;
	@Mock ShipmentsService shipmentsService;

	@Mock CompletableFuture<Map<String, Double>>     p;
	@Mock CompletableFuture<Map<Long, String>>       t;
	@Mock CompletableFuture<Map<Long, List<String>>> s;
	@Mock CompletableFuture<Void> v;
	@Mock AllOfCompletableFutureProvider provider;

	@Mock RuntimeException e;

	List<String> pks = List.of("NL", "FR");
	List<Long>   ids = List.of(1L, 2L);

	Map<String, Double>     pricing   = Map.of("NL", 10d, "BE", 11d);
	Map<Long, String>       track     = Map.of(1L, "NEW", 2L, "COLLECTING");
	Map<Long, List<String>> shipments = Map.of(1L, List.of("box", "envelope"), 2L, List.of("pallet"));

	@BeforeEach
	public void setUp() {
		subject = new AggregationRestController(pricingService, trackService, shipmentsService);
	}

	@Test
	public void testInit() {
		// Basically test constructors args assignment
		assertSame(pricingService, getFieldValue(subject, "pricingService"));
		assertSame(trackService, getFieldValue(subject, "trackService"));
		assertSame(shipmentsService, getFieldValue(subject, "shipmentsService"));
	}

	@Test
	public void testGet() throws InterruptedException, ExecutionException {
		// Happy flow

		whenProvider();

		when(pricingService.get(pks)).thenReturn(p);
		when(trackService.get(ids)).thenReturn(t);
		when(shipmentsService.get(ids)).thenReturn(s);

		when(p.get()).thenReturn(pricing);
		when(t.get()).thenReturn(track);
		when(s.get()).thenReturn(shipments);

		Aggregation result = subject.get(pks, ids, ids);

		assertNotNull(result);
		// we test that the returned Aggregation contains the result of the futures
		assertSame(result.getPricing(), pricing);
		assertSame(result.getTrack(), track);
		assertSame(result.getShipments(), shipments);

		// check that the interactions are really what they are expected to be
		verify(p, times(1)).get();
		verify(t, times(1)).get();
		verify(s, times(1)).get();
		verify(v, times(1)).join();

		verifyNoMoreInteractions(p, t, s, v);
	}

	private void whenProvider() {
		setFieldValue(subject, "provider", provider);

		// the provider is a workaround to encapsulate the creation of the lock future
		when(provider.allOf(p, s, t)).thenReturn(v);
	}

	@Test
	public void testGetThrowsWhenException() throws InterruptedException, ExecutionException {
		whenProvider();

		when(p.get()).thenThrow(e);
		// a little simplistic
		assertThrows(RuntimeException.class, () -> subject.get(pks, ids, ids));
	}

}
