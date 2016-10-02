package com.github.jparkie.promise;

import java.util.concurrent.TimeUnit;

/**
 * A Promise is an object which can be completed with a value or failed with an exception.
 * Furthermore, a promise can be cancelled, to prevent the propagation of its completion.
 * The cancelling of a promise does not undo the propagation of a completion
 * after the promise already completed. As well, it does not undo the completion.
 * However, the cancelling of a promise prevents new propagation of completion.
 *
 * It is a unit of asynchronous computation; thus, {@link Function}s and {@link Action}s execute
 * on a {@link Scheduler}. Accordingly, a promise is thread-safe.
 * @param <T> The type of the value promised to be available now, or in the future, or never.
 */
public interface Promise<T> {
    /**
     * Returns whether the promise has been cancelled.
     * @return If cancelled, true, else, false.
     */
    boolean isCancelled();

    /**
     * Returns whether the promise has been completed. A cancellation does not affect
     * the status of completion.
     * @return If set() or setError() has been called, true, else false.
     */
    boolean isDone();

    /**
     * Returns whether the promise has completed and has no errors .
     * @return If isDone() and has no errors, true, else false.
     */
    boolean isSuccessful();

    /**
     * Cancels the propagation of completion of the promise. This operation is idempotent.
     * The propagation of cancelling will occur only once.
     */
    void cancel();

    /**
     * Await the completion or the cancellation of the promise.
     * @throws InterruptedException If the thread awaiting on the promise is interrupted,
     * an exception is thrown.
     */
    void await() throws InterruptedException;

    /**
     * Await the completion or the cancellation of the promise at most the time specified.
     * @param timeout The duration at most to await.
     * @param unit The unit of duration to await.
     * @return If the promise completed or cancelled under the timeout, true, else, false.
     * @throws InterruptedException If the thread awaiting on the promise is interrupted,
     * an exception is thrown.
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Returns the value promised upon completion.
     * If cancelled, returns the current view of the "value": null or otherwise.
     * @return The value promised upon completion.
     * @throws IllegalStateException If this method is called before the completion of the promise,
     * an exception is thrown.
     */
    T get() throws IllegalStateException;

    /**
     * Returns the error promised upon completion.
     * If cancelled, returns the current view of the "error": null or otherwise.
     * @return The error promised upon completion.
     * @throws IllegalStateException If this method is called before the completion of the promise,
     * an exception is thrown.
     */
    Throwable getError() throws IllegalStateException;

    /**
     * Completes the promise with a value.
     * Please avoid propagating nulls. Nulls are allowed due to the nature of cancelled promises.
     * @param value The value to complete the promise.
     * @throws IllegalStateException If this method is called after the completion of the promise,
     * an exception is thrown.
     */
    void set(T value) throws IllegalStateException;

    /**
     * Completes the promise with an error.
     * Please avoid propagating nulls. Nulls are allowed due to the nature of cancelled promises.
     * @param error The error to complete the promise.
     * @throws IllegalStateException If this method is called after the completion of the promise,
     * an exception is thrown.
     */
    void setError(Throwable error) throws IllegalStateException;

    /**
     * Returns a new transformed promise specified by the function
     * upon the completion of this promise.
     * @param scheduler The scheduler to call the function.
     * @param function A function to transform the current promise into another promise.
     * @param <U> The new type of promise.
     * @return The transformed promise.
     */
    <U> Promise<U> then(Scheduler scheduler, Function<T, U> function);

    /**
     * Calls the action on the promise upon the completion of this promise.
     * @param scheduler The scheduler to call the action.
     * @param action The action to call on the current promise.
     */
    void then(Scheduler scheduler, Action<T> action);
}
