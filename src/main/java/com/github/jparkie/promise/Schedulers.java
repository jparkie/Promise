package com.github.jparkie.promise;

import java.util.concurrent.ExecutorService;

/**
 * A companion class for {@link Scheduler}.
 *
 * Provides various methods for accessing schedulers.
 */
public final class Schedulers {
    private Schedulers() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static Scheduler newSimpleScheduler() {
        return new SimpleScheduler();
    }

    public static Scheduler newExecutorServiceScheduler(ExecutorService executorService) {
        return new ExecutorServiceScheduler(executorService);
    }

    private static class SimpleScheduler implements Scheduler {
        private SimpleScheduler() {
            // Do Nothing.
        }

        @Override
        public <T> void schedule(Action<T> action, Promise<T> promise) {
            action.call(promise);
        }

        @Override
        public <T> void cancel(Action<T> action) {
            action.cancel();
        }
    }

    private static class ExecutorServiceScheduler implements Scheduler {
        private final ExecutorService executorService;

        ExecutorServiceScheduler(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public <T> void schedule(final Action<T> action, final Promise<T> promise) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    action.call(promise);
                }
            });
        }

        @Override
        public <T> void cancel(final Action<T> action) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    action.cancel();
                }
            });
        }
    }
}
