package com.github.jparkie.promise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A companion class for {@link Promise}.
 *
 * Provides various methods for creating promises.
 */
public final class Promises {
    private Promises() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Returns a promise scheduled to be resolved by the specified action.
     * Upon cancellation, the specified cancel procedure will be called.
     * @param scheduler The scheduler to call the action.
     * @param onScheduleAction The action to resolve the provided promise.
     * @param <T> The type of the value promised to be available now, or in the future, or never.
     * @return The created promise.
     */
    public static <T> Promise<T> create(final Scheduler scheduler, final Action<T> onScheduleAction) {
        final Promise<T> createPromise = new DefaultPromise<T>();
        createPromise.then(scheduler, new Action<T>() {
            @Override
            public void call(Promise<T> promise) {
                // Do Nothing.
            }

            @Override
            public void cancel() {
                onScheduleAction.cancel();
            }
        });

        scheduler.schedule(onScheduleAction, createPromise);

        return createPromise;
    }

    /**
     * Returns a new unresolved promise.
     * @param <T> The type of the value promised to be available now, or in the future, or never.
     * @return The unresolved promise.
     */
    public static <T> Promise<T> promise() {
        return new DefaultPromise<T>();
    }

    /**
     * Returns a new promise which wraps the provided value.
     * @param value The value to lift into a promise.
     * @param <T> The type of the value promised to be available now, or in the future, or never.
     * @return The new promise.
     */
    public static <T> Promise<T> value(T value) {
        return new ValuePromise<T>(value);
    }

    /**
     * Returns a new promise which wraps the provided error.
     * @param error The error to lift into a promise.
     * @param <T> The type of the value promised to be available now, or in the future, or never.
     * @return The new promise.
     */
    public static <T> Promise<T> error(Throwable error) {
        return new ErrorPromise<T>(error);
    }

    private static <T, U> Promise<U> internalThen(
            final Scheduler scheduler,
            final Function<T, U> function,
            final Promise<T> promise) {
        final Promise<U> deferredPromise = new DefaultPromise<U>();
        if (promise.isCancelled()) {
            deferredPromise.cancel();
        } else {
            promise.then(scheduler, new Action<T>() {
                @Override
                public void call(Promise<T> selfPromise) {
                    final Promise<U> calledPromise = function.call(selfPromise);
                    calledPromise.then(scheduler, new Action<U>() {
                        @Override
                        public void call(Promise<U> completedPromise) {
                            if (completedPromise.isSuccessful()) {
                                deferredPromise.set(completedPromise.get());
                            } else {
                                deferredPromise.setError(completedPromise.getError());
                            }
                        }

                        @Override
                        public void cancel() {
                            if (!deferredPromise.isCancelled()) {
                                deferredPromise.cancel();
                            }
                        }
                    });
                }

                @Override
                public void cancel() {
                    if (!deferredPromise.isCancelled()) {
                        deferredPromise.cancel();
                    }
                }
            });
        }

        return deferredPromise;
    }

    private static final class ValuePromise<T> implements Promise<T> {
        private final T value;

        private volatile boolean cancel;

        private ValuePromise(T value) {
            this.value = value;
        }

        @Override
        public boolean isCancelled() {
            return cancel;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }

        @Override
        public void cancel() {
            cancel = true;
        }

        @Override
        public void await() throws InterruptedException {
            // Do Nothing.
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public T get() throws IllegalStateException {
            return value;
        }

        @Override
        public Throwable getError() throws IllegalStateException {
            return null;
        }

        @Override
        public void set(T value) throws IllegalStateException {
            if (isCancelled()) {
                return;
            }

            throw new IllegalStateException();
        }

        @Override
        public void setError(Throwable error) throws IllegalStateException {
            if (isCancelled()) {
                return;
            }

            throw new IllegalStateException();
        }

        @Override
        public <U> Promise<U> then(Scheduler scheduler, Function<T, U> function) {
            return internalThen(scheduler, function, this);
        }

        @Override
        public void then(Scheduler scheduler, Action<T> action) {
            if (isCancelled()) {
                scheduler.cancel(action);
                return;
            }

            scheduler.schedule(action, this);
        }
    }

    private static final class ErrorPromise<T> implements Promise<T> {
        private final Throwable error;

        private volatile boolean cancel;

        private ErrorPromise(Throwable error) {
            this.error = error;
        }

        @Override
        public boolean isCancelled() {
            return cancel;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

        @Override
        public void cancel() {
            cancel = true;
        }

        @Override
        public void await() throws InterruptedException {
            // Do Nothing.
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public T get() throws IllegalStateException {
            return null;
        }

        @Override
        public Throwable getError() throws IllegalStateException {
            return error;
        }

        @Override
        public void set(T value) throws IllegalStateException {
            if (isCancelled()) {
                return;
            }

            throw new IllegalStateException();
        }

        @Override
        public void setError(Throwable error) throws IllegalStateException {
            if (isCancelled()) {
                return;
            }

            throw new IllegalStateException();
        }

        @Override
        public <U> Promise<U> then(Scheduler scheduler, Function<T, U> function) {
            return internalThen(scheduler, function, this);
        }

        @Override
        public void then(Scheduler scheduler, Action<T> action) {
            if (isCancelled()) {
                scheduler.cancel(action);
                return;
            }

            scheduler.schedule(action, this);
        }
    }

    private static final class DefaultPromise<T> implements Promise<T> {
        private final Object promiseLock = new Object();
        private final AtomicBoolean promiseFlag = new AtomicBoolean(false);
        private final CountDownLatch awaitLatch = new CountDownLatch(1);
        private final List<ActionContext<T>> actionContexts = new ArrayList<ActionContext<T>>();

        private volatile T value;
        private volatile Throwable error;
        private volatile boolean cancel;

        private DefaultPromise() {
            // Do Nothing.
        }

        @Override
        public boolean isCancelled() {
            return cancel;
        }

        @Override
        public boolean isDone() {
            return promiseFlag.get();
        }

        @Override
        public boolean isSuccessful() {
            return isDone() && error == null;
        }

        @Override
        public void cancel() {
            cancel = true;

            final List<ActionContext<T>> temporaryActionContexts;
            synchronized (promiseLock) {
                temporaryActionContexts = new ArrayList<ActionContext<T>>(actionContexts);
                actionContexts.clear();
            }

            for (ActionContext<T> actionContext : temporaryActionContexts) {
                actionContext.scheduler.cancel(actionContext.action);
            }

            awaitLatch.countDown();
        }

        @Override
        public void await() throws InterruptedException {
            awaitLatch.await();
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return awaitLatch.await(timeout, unit);
        }

        @Override
        public T get() throws IllegalStateException {
            if (!isCancelled() && !isDone()) {
                throw new IllegalStateException();
            }

            return value;
        }

        @Override
        public Throwable getError() throws IllegalStateException {
            if (!isCancelled() && !isDone()) {
                throw new IllegalStateException();
            }

            return error;
        }

        @Override
        public void set(T value) {
            if (isCancelled()) {
                return;
            }

            final List<ActionContext<T>> temporaryActionContexts;
            synchronized (promiseLock) {
                if (isDone()) {
                    throw new IllegalStateException();
                }

                this.value = value;
                this.error = null;
                this.promiseFlag.compareAndSet(false, true);

                temporaryActionContexts = new ArrayList<ActionContext<T>>(actionContexts);
                actionContexts.clear();
            }

            for (ActionContext<T> actionContext : temporaryActionContexts) {
                actionContext.scheduler.schedule(actionContext.action, this);
            }

            awaitLatch.countDown();
        }

        @Override
        public void setError(Throwable error) {
            if (isCancelled()) {
                return;
            }

            final List<ActionContext<T>> temporaryActionContexts;
            synchronized (promiseLock) {
                if (isDone()) {
                    throw new IllegalStateException();
                }

                this.value = null;
                this.error = error;
                this.promiseFlag.compareAndSet(false, true);

                temporaryActionContexts = new ArrayList<ActionContext<T>>(actionContexts);
                actionContexts.clear();
            }

            for (ActionContext<T> actionContext : temporaryActionContexts) {
                actionContext.scheduler.schedule(actionContext.action, this);
            }

            awaitLatch.countDown();
        }

        @Override
        public <U> Promise<U> then(Scheduler scheduler, Function<T, U> function) {
            return internalThen(scheduler, function, this);
        }

        @Override
        public void then(Scheduler scheduler, Action<T> action) {
            if (isCancelled()) {
                scheduler.cancel(action);
                return;
            }

            synchronized (promiseLock) {
                if (!isDone()) {
                    actionContexts.add(new ActionContext<T>(scheduler, action));
                } else {
                    scheduler.schedule(action, this);
                }
            }
        }

        private static final class ActionContext<T> {
            private final Scheduler scheduler;
            private final Action<T> action;

            private ActionContext(Scheduler scheduler, Action<T> action) {
                this.scheduler = scheduler;
                this.action = action;
            }
        }
    }
}
