package com.github.jparkie.promise;

import com.github.jparkie.promise.utils.MemoryLeakVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class PromiseUnitTest {
    private Timer timer;

    @Before
    public void setup() {
        timer = new Timer();
    }

    @After
    public void teardown() {
        timer.cancel();
        timer.purge();
        timer = null;
    }

    @Test
    public void testCreate1() {
        final AtomicBoolean callFlag = new AtomicBoolean(false);
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        final Promise<String> testPromise = Promises.create(
                Schedulers.newSimpleScheduler(),
                new Action<String>() {
                    @Override
                    public void call(Promise<String> promise) {
                        callFlag.set(true);
                        promise.set("TEST");
                    }

                    @Override
                    public void cancel() {
                        cancelFlag.set(true);
                    }
                });

        try {
            testPromise.await(2, TimeUnit.SECONDS);

            assertTrue(callFlag.get());
            assertFalse(cancelFlag.get());
            assertFalse(testPromise.isCancelled());
            assertTrue(testPromise.isDone());
            assertTrue(testPromise.isSuccessful());
            assertEquals("TEST", testPromise.get());
            assertNull(testPromise.getError());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testCreate2() {
        final AtomicBoolean callFlag = new AtomicBoolean(false);
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        final Promise<String> testPromise = Promises.create(
                Schedulers.newSimpleScheduler(),
                new Action<String>() {
                    @Override
                    public void call(final Promise<String> promise) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                callFlag.set(true);
                                promise.set("TEST");
                            }
                        }, 100);
                    }

                    @Override
                    public void cancel() {
                        cancelFlag.set(true);
                    }
                });

        testPromise.cancel();

        assertFalse(callFlag.get());
        assertTrue(cancelFlag.get());
        assertTrue(testPromise.isCancelled());
        assertFalse(testPromise.isDone());
        assertFalse(testPromise.isSuccessful());
        assertNull(testPromise.get());
        assertNull(testPromise.getError());
    }

    @Test
    public void testPromise() {
        final Promise<String> testPromise = Promises.promise();

        assertFalse(testPromise.isCancelled());
        assertFalse(testPromise.isDone());
        assertFalse(testPromise.isSuccessful());
    }

    @Test
    public void testValue() {
        final Promise<String> testPromise = Promises.value("TEST");

        assertFalse(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertTrue(testPromise.isSuccessful());
        assertEquals("TEST", testPromise.get());
        assertNull(testPromise.getError());
    }

    @Test
    public void testError() {
        final Throwable error = new NoSuchElementException();
        final Promise<String> testPromise = Promises.error(error);

        assertFalse(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertFalse(testPromise.isSuccessful());
        assertNull(testPromise.get());
        assertEquals(error, testPromise.getError());
    }

    @Test
    public void testCancel1() {
        final Promise<String> testPromise = Promises.promise();
        testPromise.cancel();

        assertTrue(testPromise.isCancelled());
        assertFalse(testPromise.isDone());
        assertFalse(testPromise.isSuccessful());
        assertNull(testPromise.get());
        assertNull(testPromise.getError());
    }

    @Test
    public void testCancel2() {
        final Promise<String> testPromise = Promises.promise();
        final AtomicBoolean callFlag = new AtomicBoolean(false);
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        testPromise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                callFlag.set(true);
            }

            @Override
            public void cancel() {
                cancelFlag.set(true);
            }
        });
        testPromise.set("TEST");
        testPromise.cancel();

        assertTrue(callFlag.get());
        assertFalse(cancelFlag.get());
        assertTrue(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertTrue(testPromise.isSuccessful());
        assertEquals("TEST", testPromise.get());
        assertNull(testPromise.getError());
    }

    @Test
    public void testCancel3() {
        final Promise<String> testPromise = Promises.promise();
        final AtomicBoolean callFlag = new AtomicBoolean(false);
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        testPromise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                callFlag.set(true);
            }

            @Override
            public void cancel() {
                cancelFlag.set(true);
            }
        });
        testPromise.cancel();
        testPromise.set("TEST");

        assertFalse(callFlag.get());
        assertTrue(cancelFlag.get());
        assertTrue(testPromise.isCancelled());
        assertFalse(testPromise.isDone());
        assertFalse(testPromise.isSuccessful());
        assertNull(testPromise.get());
        assertNull(testPromise.getError());
    }

    @Test
    public void testAwait1() {
        final Promise<String> testPromise = Promises.promise();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testPromise.set("TEST");
            }
        }, 100);

        try {
            testPromise.await(2, TimeUnit.SECONDS);

            assertFalse(testPromise.isCancelled());
            assertTrue(testPromise.isDone());
            assertTrue(testPromise.isSuccessful());
            assertEquals("TEST", testPromise.get());
            assertNull(testPromise.getError());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testAwait2() {
        final Promise<String> testPromise = Promises.promise();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testPromise.set("TEST");
            }
        }, TimeUnit.DAYS.toMillis(1));

        try {
            final boolean awaitFlag = testPromise.await(100, TimeUnit.MILLISECONDS);

            assertFalse(awaitFlag);
            assertFalse(testPromise.isCancelled());
            assertFalse(testPromise.isDone());
            assertFalse(testPromise.isSuccessful());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGet() {
        final Promise<String> testPromise = Promises.promise();

        testPromise.get();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test(expected = IllegalStateException.class)
    public void testGetError() {
        final Promise<String> testPromise = Promises.promise();

        testPromise.getError();
    }

    @Test
    public void testSet() {
        final Promise<String> testPromise = Promises.promise();

        testPromise.set("TEST");

        assertFalse(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertTrue(testPromise.isSuccessful());
        assertEquals("TEST", testPromise.get());
        assertNull(testPromise.getError());
    }

    @Test
    public void testSetError() {
        final Promise<String> testPromise = Promises.promise();
        final RuntimeException testException = new RuntimeException("TEST");

        testPromise.setError(testException);

        assertFalse(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertFalse(testPromise.isSuccessful());
        assertNull(testPromise.get());
        assertEquals(testException, testPromise.getError());
    }

    @Test
    public void testThen1() {
        final Promise<String> testPromise = Promises.promise();
        final CountDownLatch callLatch = new CountDownLatch(3);
        final CountDownLatch cancelLatch = new CountDownLatch(3);
        testPromise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                callLatch.countDown();
            }

            @Override
            public void cancel() {
                cancelLatch.countDown();
            }
        });
        testPromise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                callLatch.countDown();
            }

            @Override
            public void cancel() {
                cancelLatch.countDown();
            }
        });
        testPromise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                callLatch.countDown();
            }

            @Override
            public void cancel() {
                cancelLatch.countDown();
            }
        });

        testPromise.set("TEST");

        assertEquals(0, callLatch.getCount());
        assertEquals(3, cancelLatch.getCount());
        assertFalse(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertTrue(testPromise.isSuccessful());
        assertEquals(testPromise.get(), "TEST");
        assertNull(testPromise.getError());
    }

    @Test
    public void testThen2() {
        final Promise<String> testPromise = Promises.promise();
        final CountDownLatch callLatch = new CountDownLatch(3);

        final Promise<String> firstPromise = testPromise
                .then(Schedulers.newSimpleScheduler(), new Function<String, String>() {
                    @Override
                    public Promise<String> call(Promise<String> promise) {
                        callLatch.countDown();

                        if (promise.isSuccessful()) {
                            return Promises.value("FIRST");
                        } else {
                            return promise;
                        }
                    }
                });
        final Promise<String> secondPromise = firstPromise
                .then(Schedulers.newSimpleScheduler(), new Function<String, String>() {
                    @Override
                    public Promise<String> call(Promise<String> promise) {
                        callLatch.countDown();

                        if (promise.isSuccessful()) {
                            return Promises.value("SECOND");
                        } else {
                            return promise;
                        }
                    }
                });
        final Promise<String> thirdPromise = secondPromise
                .then(Schedulers.newSimpleScheduler(), new Function<String, String>() {
                    @Override
                    public Promise<String> call(Promise<String> promise) {
                        callLatch.countDown();

                        if (promise.isSuccessful()) {
                            return Promises.value("THIRD");
                        } else {
                            return promise;
                        }
                    }
                });

        testPromise.set("TEST");

        assertEquals(0, callLatch.getCount());
        assertFalse(testPromise.isCancelled());
        assertTrue(testPromise.isDone());
        assertTrue(testPromise.isSuccessful());
        assertEquals(testPromise.get(), "TEST");
        assertNull(testPromise.getError());
        assertFalse(firstPromise.isCancelled());
        assertTrue(firstPromise.isDone());
        assertTrue(firstPromise.isSuccessful());
        assertEquals(firstPromise.get(), "FIRST");
        assertNull(firstPromise.getError());
        assertFalse(secondPromise.isCancelled());
        assertTrue(secondPromise.isDone());
        assertTrue(secondPromise.isSuccessful());
        assertEquals(secondPromise.get(), "SECOND");
        assertNull(secondPromise.getError());
        assertFalse(thirdPromise.isCancelled());
        assertTrue(thirdPromise.isDone());
        assertTrue(thirdPromise.isSuccessful());
        assertEquals(thirdPromise.get(), "THIRD");
        assertNull(thirdPromise.getError());
    }

    @Test
    public void testThreadSafety() {
        final CountDownLatch callLatch = new CountDownLatch(3);
        final CountDownLatch failLatch = new CountDownLatch(3);
        final Promise<String> testPromise = Promises.promise();

        final Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    testPromise.await(1, TimeUnit.SECONDS);
                    if (testPromise.get().equals("TEST")) {
                        callLatch.countDown();
                    }
                } catch (Exception e) {
                    failLatch.countDown();
                }
            }
        };
        final Thread thread2 = new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    testPromise.await(1, TimeUnit.SECONDS);
                    if (testPromise.get().equals("TEST")) {
                        callLatch.countDown();
                    }
                } catch (Exception e) {
                    failLatch.countDown();
                }
            }
        };
        final Thread thread3 = new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    testPromise.await(1, TimeUnit.SECONDS);
                    if (testPromise.get().equals("TEST")) {
                        callLatch.countDown();
                    }
                } catch (Exception e) {
                    failLatch.countDown();
                }
            }
        };

        thread1.start();
        thread2.start();
        thread3.start();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testPromise.set("TEST");
            }
        }, 500);

        try {
            callLatch.await(2, TimeUnit.SECONDS);

            assertEquals(0, callLatch.getCount());
            assertEquals(3, failLatch.getCount());
            assertFalse(testPromise.isCancelled());
            assertTrue(testPromise.isDone());
            assertTrue(testPromise.isSuccessful());
            assertEquals(testPromise.get(), "TEST");
            assertNull(testPromise.getError());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testThreadIsolation() {
        final CountDownLatch callLatch = new CountDownLatch(3);
        final CountDownLatch threadLatch = new CountDownLatch(3);
        final AtomicBoolean incorrectThreadFlag = new AtomicBoolean(false);
        final Promise<String> testPromise = Promises.promise();

        final ThreadFactory threadFactory1 = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TEST_THREAD_ISOLATION_1");
            }
        };
        final ThreadFactory threadFactory2 = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TEST_THREAD_ISOLATION_2");
            }
        };
        final ThreadFactory threadFactory3 = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TEST_THREAD_ISOLATION_3");
            }
        };
        final Scheduler threadScheduler1 = Schedulers.newExecutorServiceScheduler(Executors.newSingleThreadExecutor(threadFactory1));
        final Scheduler threadScheduler2 = Schedulers.newExecutorServiceScheduler(Executors.newSingleThreadExecutor(threadFactory2));
        final Scheduler threadScheduler3 = Schedulers.newExecutorServiceScheduler(Executors.newSingleThreadExecutor(threadFactory3));

        final Promise<String> firstPromise = testPromise
                .then(threadScheduler1, new Function<String, String>() {
                    @Override
                    public Promise<String> call(Promise<String> promise) {
                        callLatch.countDown();

                        if (Thread.currentThread().getName().equals("TEST_THREAD_ISOLATION_1")) {
                            threadLatch.countDown();
                        } else {
                            incorrectThreadFlag.set(true);
                        }

                        if (promise.isSuccessful()) {
                            return Promises.value("FIRST");
                        } else {
                            return promise;
                        }
                    }
                });
        final Promise<String> secondPromise = firstPromise
                .then(threadScheduler2, new Function<String, String>() {
                    @Override
                    public Promise<String> call(Promise<String> promise) {
                        callLatch.countDown();

                        if (Thread.currentThread().getName().equals("TEST_THREAD_ISOLATION_2")) {
                            threadLatch.countDown();
                        } else {
                            incorrectThreadFlag.set(true);
                        }

                        if (promise.isSuccessful()) {
                            return Promises.value("SECOND");
                        } else {
                            return promise;
                        }
                    }
                });
        final Promise<String> thirdPromise = secondPromise
                .then(threadScheduler3, new Function<String, String>() {
                    @Override
                    public Promise<String> call(Promise<String> promise) {
                        callLatch.countDown();

                        if (Thread.currentThread().getName().equals("TEST_THREAD_ISOLATION_3")) {
                            threadLatch.countDown();
                        } else {
                            incorrectThreadFlag.set(true);
                        }

                        if (promise.isSuccessful()) {
                            return Promises.value("THIRD");
                        } else {
                            return promise;
                        }
                    }
                });

        testPromise.set("TEST");

        try {
            thirdPromise.await(2, TimeUnit.SECONDS);

            assertEquals(0, callLatch.getCount());
            assertEquals(0, threadLatch.getCount());
            assertFalse(incorrectThreadFlag.get());
            assertFalse(testPromise.isCancelled());
            assertTrue(testPromise.isDone());
            assertTrue(testPromise.isSuccessful());
            assertEquals(testPromise.get(), "TEST");
            assertNull(testPromise.getError());
            assertFalse(firstPromise.isCancelled());
            assertTrue(firstPromise.isDone());
            assertTrue(firstPromise.isSuccessful());
            assertEquals(firstPromise.get(), "FIRST");
            assertNull(firstPromise.getError());
            assertFalse(secondPromise.isCancelled());
            assertTrue(secondPromise.isDone());
            assertTrue(secondPromise.isSuccessful());
            assertEquals(secondPromise.get(), "SECOND");
            assertNull(secondPromise.getError());
            assertFalse(thirdPromise.isCancelled());
            assertTrue(thirdPromise.isDone());
            assertTrue(thirdPromise.isSuccessful());
            assertEquals(thirdPromise.get(), "THIRD");
            assertNull(thirdPromise.getError());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    public void testActionGarbageCollected() {
        final Promise<String> testPromise = Promises.promise();
        Action<String> testAction = new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                // Do Nothing.
            }

            @Override
            public void cancel() {
                // Do Nothing.
            }
        };
        final MemoryLeakVerifier<Action<String>> testMemoryLeakVerifier = new MemoryLeakVerifier<Action<String>>(testAction);
        testPromise.then(Schedulers.newSimpleScheduler(), testAction);
        testPromise.cancel();
        testAction = null;
        testMemoryLeakVerifier.assertGarbageCollected();
    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    public void testThreadedActionGarbageCollected() {
        final Promise<String> testPromise = Promises.promise();
        Action<String> testAction = new Action<String>() {
            @Override
            public void call(Promise<String> promise) {
                // Do Nothing.
            }

            @Override
            public void cancel() {
                // Do Nothing.
            }
        };
        final Thread testThread = new Thread() {
            @Override
            public void run() {
                super.run();
                testPromise.cancel();
            }
        };
        final MemoryLeakVerifier<Action<String>> testMemoryLeakVerifier = new MemoryLeakVerifier<Action<String>>(testAction);
        testPromise.then(Schedulers.newSimpleScheduler(), testAction);
        testThread.start();
        try {
            testThread.join();
        } catch (InterruptedException e) {
            fail();
        }
        testAction = null;
        testMemoryLeakVerifier.assertGarbageCollected();
    }
}
