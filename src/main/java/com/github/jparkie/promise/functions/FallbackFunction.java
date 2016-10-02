package com.github.jparkie.promise.functions;

import com.github.jparkie.promise.Function;
import com.github.jparkie.promise.Promise;

public final class FallbackFunction<T> implements Function<T, T> {
    private final Promise<T> fallbackPromise;

    public FallbackFunction(Promise<T> fallbackPromise) {
        this.fallbackPromise = fallbackPromise;
    }

    @Override
    public Promise<T> call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            return promise;
        } else {
            return fallbackPromise;
        }
    }
}
