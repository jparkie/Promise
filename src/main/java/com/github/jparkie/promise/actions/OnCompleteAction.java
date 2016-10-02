package com.github.jparkie.promise.actions;

import com.github.jparkie.promise.Action;
import com.github.jparkie.promise.Promise;

public abstract class OnCompleteAction<T> implements Action<T> {
    @Override
    public void call(Promise<T> promise) {
        if (promise.isSuccessful()) {
            onSuccess(promise.get());
        } else {
            onFailure(promise.getError());
        }
    }

    @Override
    public void cancel() {
        // Do Nothing.
    }

    public abstract void onSuccess(T value);

    public abstract void onFailure(Throwable error);
}
