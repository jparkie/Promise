package com.github.jparkie.promise;

/**
 * A Scheduler executes an {@link Action} on a {@link Promise}.
 *
 * Any underlying resource should be managed separately from the scheduler. Accordingly,
 * this interface provides no starting and stopping procedures.
 */
public interface Scheduler {
    /**
     * Propagates the call of an action on the promise.
     * @param action The action to call on the promise.
     * @param promise The promise that the action calls.
     * @param <T> The type of the promise.
     */
    <T> void schedule(Action <T> action, Promise<T> promise);

    /**
     * Propagates the cancel of an action.
     * @param action The action to cancel.
     * @param <T> The type of the action.
     */
    <T> void cancel(Action <T> action);
}
