package com.github.jparkie.promise.utils;

import java.lang.ref.WeakReference;

import static org.junit.Assert.assertNull;

/**
 * Adapted from http://stackoverflow.com/a/7410460.
 */
public final class MemoryLeakVerifier<T> {
    private static final int MAX_GC_ITERATIONS = 50;
    private static final int GC_SLEEP_DURATION_MS = 100;

    private final WeakReference<T> reference;

    public MemoryLeakVerifier(T object) {
        this.reference = new WeakReference<T>(object);
    }

    public T getObject() {
        return reference.get();
    }

    public final void assertGarbageCollected() {
        assertGarbageCollected(null);
    }

    public final void assertGarbageCollected(String message) {
        final Runtime runtime = Runtime.getRuntime();
        for (int counter = 1; counter <= MAX_GC_ITERATIONS; counter++) {
            runtime.runFinalization();
            runtime.gc();
            if (getObject() == null) {
                break;
            }
            try {
                Thread.sleep(GC_SLEEP_DURATION_MS);
            } catch (InterruptedException e) {
                // Do Nothing.
            }
        }
        assertNull(message, getObject());
    }
}
