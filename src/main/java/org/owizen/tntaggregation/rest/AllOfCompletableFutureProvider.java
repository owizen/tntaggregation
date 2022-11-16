package org.owizen.tntaggregation.rest;

import java.util.concurrent.CompletableFuture;

public class AllOfCompletableFutureProvider {


	public CompletableFuture<Void> allOf(CompletableFuture<?>... futures) {
		return CompletableFuture.allOf(futures);
	}

}
