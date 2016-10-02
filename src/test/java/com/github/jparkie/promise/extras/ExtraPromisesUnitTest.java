package com.github.jparkie.promise.extras;

import android.support.v4.util.Pair;

import com.github.jparkie.promise.Promise;
import com.github.jparkie.promise.Promises;
import com.github.jparkie.promise.Schedulers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ExtraPromisesUnitTest {
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

    @SuppressWarnings("unchecked")
    @Test
    public void testFirstCompletedOf() {
        final Promise<String> firstPromise = Promises.promise();
        final Promise<String> secondPromise = Promises.promise();
        final Promise<String> thirdPromise = Promises.promise();

        final Promise<String> firstCompletedOfPromise = ExtraPromises.firstCompletedOf(
                Schedulers.newSimpleScheduler(),
                firstPromise,
                secondPromise,
                thirdPromise);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                firstPromise.set("FIRST");
            }
        }, 100);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                secondPromise.set("SECOND");
            }
        }, 200);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                thirdPromise.set("THIRD");
            }
        }, 300);

        try {
            firstCompletedOfPromise.await(2, TimeUnit.SECONDS);

            assertFalse(firstCompletedOfPromise.isCancelled());
            assertTrue(firstCompletedOfPromise.isDone());
            assertTrue(firstCompletedOfPromise.isSuccessful());
            assertEquals("FIRST", firstCompletedOfPromise.get());
            assertNull(firstCompletedOfPromise.getError());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testWhenAll() {
        final Promise<String> firstPromise = Promises.promise();
        final Promise<Integer> secondPromise = Promises.promise();
        final Promise<Boolean> thirdPromise = Promises.promise();

        final Promise<Void> whenAllPromise = ExtraPromises.whenAll(
                Schedulers.newSimpleScheduler(),
                firstPromise,
                secondPromise,
                thirdPromise);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                firstPromise.set("FIRST");
            }
        }, 100);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                secondPromise.set(1);
            }
        }, 200);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                thirdPromise.set(true);
            }
        }, 300);

        try {
            whenAllPromise.await(2, TimeUnit.SECONDS);

            assertFalse(whenAllPromise.isCancelled());
            assertTrue(whenAllPromise.isDone());
            assertTrue(whenAllPromise.isSuccessful());
            assertNull(whenAllPromise.get());
            assertNull(whenAllPromise.getError());
            assertEquals("FIRST", firstPromise.get());
            assertEquals(Integer.valueOf(1), secondPromise.get());
            assertEquals(true, thirdPromise.get());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testZip() {
        final Promise<String> leftPromise = Promises.promise();
        final Promise<Integer> rightPromise = Promises.promise();

        final Promise<Pair<String, Integer>> zipPromise = ExtraPromises.zip(
                Schedulers.newSimpleScheduler(),
                leftPromise,
                rightPromise);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                leftPromise.set("FIRST");
            }
        }, 100);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                rightPromise.set(1);
            }
        }, 200);

        try {
            zipPromise.await(2, TimeUnit.DAYS);

            assertFalse(zipPromise.isCancelled());
            assertTrue(zipPromise.isDone());
            assertTrue(zipPromise.isSuccessful());
            assertEquals(Pair.create("FIRST", 1), zipPromise.get());
            assertNull(zipPromise.getError());
        } catch (InterruptedException e) {
            fail();
        }
    }
}
