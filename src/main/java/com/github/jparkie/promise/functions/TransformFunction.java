package com.github.jparkie.promise.functions;

import com.github.jparkie.promise.Function;
import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;

public abstract class TransformFunction<T, U> implements Function<T, U> {
    @Override
    public Promise<U> call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            return Promises.value(transformSuccess(promise.get()));
        } else {
            return Promises.error(transformThrowable(promise.getError()));
        }
    }

    public abstract U transformSuccess(T value);

    public abstract Throwable transformThrowable(Throwable error);
}