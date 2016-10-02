package com.github.jparkie.promise;

/**
 * An action to be executed upon a promise. Upon cancellation, the action should clean any resources it required.
 * @param <T> The type of promise to act upon.
 */
public interface Action<T> {
    /**
     * Performs an action on the promise.
     * @param promise The promise to act upon.
     */
    void call(Promise<T> promise);

    /**
     * Cleans any resources required by the action.
     */
    void cancel();
}
