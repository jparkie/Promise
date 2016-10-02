package com.github.jparkie.promise;

/**
 * A function to transform a promise into a new promise.
 * A function does not receive a notice of cancellation as it should not perform side-effects.
 * @param <T> The old type of promise.
 * @param <U> The new type of promise.
 */
public interface Function<T, U> {
    /**
     * Transforms an existing promise into a new promise.
     * @param promise The existing promise to transform.
     * @return The transformed promise.
     */
    Promise<U> call(Promise<T> promise);
}
