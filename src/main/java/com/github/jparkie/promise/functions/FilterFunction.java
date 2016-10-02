package com.github.jparkie.promise.functions;

import com.github.jparkie.promise.Function;
import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;

import java.util.NoSuchElementException;

public abstract class FilterFunction<T> implements Function<T, T> {
    @Override
    public Promise<T> call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            if (filter(promise.get())) {
                return promise;
            } else {
                return Promises.error(new NoSuchElementException());
            }
        } else {
            return promise;
        }
    }

    public abstract boolean filter(T value);
}
