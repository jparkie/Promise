package com.github.jparkie.promise.actions;

import com.github.jparkie.promise.Action;
import com.github.jparkie.promise.Promise;

public abstract class OnFailureAction<T> implements Action<T> {
    @Override
    public void call(Promise<T> promise) {
        if (!promise.isSuccessful()) {
            onFailure(promise.getError());
        }
    }

    @Override
    public void cancel() {
        // Do Nothing.
    }

    public abstract void onFailure(Throwable error);
}
