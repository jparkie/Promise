package com.github.jparkie.promise.functions;

import com.github.jparkie.promise.Function;
import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;

public abstract class MapFunction<T, U> implements Function<T, U> {
    @Override
    public Promise<U> call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            return Promises.value(map(promise.get()));
        } else {
            return Promises.error(promise.getError());
        }
    }

    public abstract U map(T value);
}
