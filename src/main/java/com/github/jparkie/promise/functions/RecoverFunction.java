package com.github.jparkie.promise.functions;

import com.github.jparkie.promise.Function;
import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;

public final class RecoverFunction<T> implements Function<T, T> {
    private final T recoverValue;

    public RecoverFunction(T recoverValue) {
        this.recoverValue = recoverValue;
    }

    @Override
    public Promise<T> call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            return promise;
        } else {
            return Promises.value(recoverValue);
        }
    }
}
