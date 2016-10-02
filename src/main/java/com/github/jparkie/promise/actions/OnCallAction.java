package com.github.jparkie.promise.actions;

import com.github.jparkie.promise.Action;

public abstract class OnCallAction<T> implements Action<T> {
    @Override
    public void cancel() {
        // Do Nothing.
    }
}
