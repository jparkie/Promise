package com.github.jparkie.promise.extras;

import com.github.jparkie.promise.Action;
import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;
import com.github.jparkie.promise.Scheduler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A helper class for {@link Promise}.
 *
 * Provides helper methods for coordinating multiple promises.
 */
@SuppressWarnings("unchecked")
public final class ExtraPromises {
    private ExtraPromises() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Returns a new promise which completes upon the first completion of any promise.
     *
     * The notice of completion of the provided promises are synchronzied.
     *
     * If all of the provided promises cancel,
     *  the new promise cancels.
     *
     * @param scheduler The scheduler under which to operate.
     * @param promises The promises to wait upon.
     * @param <T> The type of the value promised to be available now, or in the future, or never.
     * @return The first completed promise.
     */
    public static <T> Promise<T> firstCompletedOf(Scheduler scheduler, Promise<T>... promises) {
        final Object sequenceLock = new Object();
        final AtomicInteger cancelCounter = new AtomicInteger(promises.length);
        final Promise<T> firstCompletedPromise = Promises.promise();
        for (Promise<T> promise : promises) {
            promise.then(scheduler, new Action<T>() {
                @Override
                public void call(Promise<T> promise) {
                    synchronized (sequenceLock) {
                        if (firstCompletedPromise.isDone()) {
                            return;
                        }

                        if (promise.isSuccessful()) {
                            firstCompletedPromise.set(promise.get());
                        } else {
                            firstCompletedPromise.setError(promise.getError());
                        }
                    }
                }

                @Override
                public void cancel() {
                    if (cancelCounter.decrementAndGet() == 0 && !firstCompletedPromise.isCancelled()) {
                        firstCompletedPromise.cancel();
                    }
                }
            });
        }

        return firstCompletedPromise;
    }

    /**
     * Returns a new promise which waits for the successful completion of all provided promises.
     *
     * The notice of completion of the provided promises are synchronzied.
     *
     * If any of the provided promises cancel,
     *  the new promise cancels.
     * If any of the provided promises fail,
     *  the new promise fails with the first failure.
     * @param scheduler The scheduler under which to operate.
     * @param promises The promises to wait upon.
     * @return THe new promise which waits on all the promises.
     */
    public static Promise<Void> whenAll(Scheduler scheduler, Promise<?>... promises) {
        final Object sequenceLock = new Object();
        final AtomicInteger callCounter = new AtomicInteger(promises.length);
        final AtomicInteger cancelCounter = new AtomicInteger(promises.length);
        final Promise<Void> whenAllPromise = Promises.promise();
        for (Promise<?> promise : promises) {
            promise.then(scheduler, new Action() {
                @Override
                public void call(Promise promise) {
                    synchronized (sequenceLock) {
                        if (whenAllPromise.isDone()) {
                            return;
                        }

                        if (!promise.isSuccessful()) {
                            whenAllPromise.setError(promise.getError());
                            return;
                        }
                        if (callCounter.decrementAndGet() == 0) {
                            whenAllPromise.set(null);
                        }
                    }
                }

                @Override
                public void cancel() {
                    if (cancelCounter.decrementAndGet() == 0 && !whenAllPromise.isCancelled()) {
                        whenAllPromise.cancel();
                    }
                }
            });
        }

        return whenAllPromise;
    }

    /**
     * Zips two promises into one promise.
     * The cancellation of any of the promises will cancel the zipped promise.
     * The failure of any of the promises will propagate the first failure.
     * @param scheduler The scheduler under which to operate.
     * @param tPromise The left promise to zip.
     * @param uPromise The right promise to zip.
     * @param <T> The left type.
     * @param <U> The right type.
     * @return The zipped promise of the left and the right.
     */
    public static <T, U> Promise<Pair<T, U>> zip(Scheduler scheduler, Promise<T> tPromise, Promise<U> uPromise) {
        final Object sequenceLock = new Object();
        final AtomicBoolean leftFlag = new AtomicBoolean(false);
        final AtomicBoolean rightFlag = new AtomicBoolean(false);
        final AtomicReference<T> leftReference = new AtomicReference<T>(null);
        final AtomicReference<U> rightReference = new AtomicReference<U>(null);
        final Promise<Pair<T, U>> zippedPromise = Promises.promise();
        tPromise.then(scheduler, new Action<T>() {
            @Override
            public void call(Promise<T> promise) {
                synchronized (sequenceLock) {
                    if (zippedPromise.isDone()) {
                        return;
                    }
                    if (!promise.isSuccessful()) {
                        zippedPromise.setError(promise.getError());
                        return;
                    }

                    leftFlag.set(true);
                    leftReference.set(promise.get());

                    if (leftFlag.get() && rightFlag.get()) {
                        zippedPromise.set(Pair.create(leftReference.get(), rightReference.get()));
                    }
                }
            }

            @Override
            public void cancel() {
                if (!zippedPromise.isCancelled()) {
                    zippedPromise.cancel();
                }
            }
        });
        uPromise.then(scheduler, new Action<U>() {
            @Override
            public void call(Promise<U> promise) {
                synchronized (sequenceLock) {
                    if (zippedPromise.isDone()) {
                        return;
                    }
                    if (!promise.isSuccessful()) {
                        zippedPromise.setError(promise.getError());
                        return;
                    }

                    rightFlag.set(true);
                    rightReference.set(promise.get());

                    if (leftFlag.get() && rightFlag.get()) {
                        zippedPromise.set(Pair.create(leftReference.get(), rightReference.get()));
                    }
                }
            }

            @Override
            public void cancel() {
                if (!zippedPromise.isCancelled()) {
                    zippedPromise.cancel();
                }
            }
        });

        return zippedPromise;
    }
}
