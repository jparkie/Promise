package com.github.jparkie.promise.actions;

import com.github.jparkie.promise.Action;
import com.github.jparkie.promise.Promise;

public abstract class OnSuccessAction<T> implements Action<T> {
    @Override
    public void call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            onSuccess(promise.get());
        }
    }

    @Override
    public void cancel() {
        // Do Nothing.
    }

    public abstract void onSuccess(T value);
}
