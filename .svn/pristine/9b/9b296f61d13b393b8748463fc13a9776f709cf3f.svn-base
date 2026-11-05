package com.knot.provisioning.utill;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.ThreadContext;

public class MdcAwareCallable<V> implements Callable<V> {
	private final Callable<V> delegate;
	private final Map<String, String> contextMap;
 
	public MdcAwareCallable(Callable<V> delegate) {
		this.delegate = delegate;
		// Capture MDC from the submitting thread
		this.contextMap = ThreadContext.getImmutableContext();
	}
 
	@Override
	public V call() throws Exception {
		try {
			// Apply MDC into worker thread
			ThreadContext.putAll(contextMap);
			return delegate.call();
		} finally {
			// Always clean up
			ThreadContext.clearAll();
		}
	}
}
