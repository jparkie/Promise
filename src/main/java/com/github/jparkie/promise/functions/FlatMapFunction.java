package com.github.jparkie.promise.functions;

import com.github.jparkie.promise.Function;
import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;

public abstract class FlatMapFunction<T, U> implements Function<T, U> {
    @Override
    public Promise<U> call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            return flatMap(promise.get());
        } else {
            return Promises.error(promise.getError());
        }
    }

    public abstract Promise<U> flatMap(T value);
}

