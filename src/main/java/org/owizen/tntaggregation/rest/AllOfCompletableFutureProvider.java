package org.owizen.tntaggregation.rest;

import java.util.concurrent.CompletableFuture;

/**
 * Workaround to encapsulate the call to the static method allOf
 * and make the unit testing easier (although not tested...)
 */
public class AllOfCompletableFutureProvider {


	public CompletableFuture<Void> allOf(CompletableFuture<?>... futures) {
		return CompletableFuture.allOf(futures);
	}

}
