package com.github.jparkie.promise.actions;

import com.github.jparkie.promise.Action;
import com.github.jparkie.promise.Promise;

public abstract class OnCancelAction<T> implements Action<T> {
    @Override
    public void call(Promise<T> promise) {
        // Do Nothing.
    }
}