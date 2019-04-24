package org.jenkinsci.plugins.globalEventsPlugin;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SchedulerTest {

    public static final int WAIT_A_LITTLE_BIT = 10;

    @Test
    public void testRunner() {
        final AtomicInteger counter = new AtomicInteger();
        final Scheduler scheduler = new Scheduler(new Runner(counter), TimeUnit.MILLISECONDS);

        scheduler.run(1);
        waitALittleBit();

        assertNotEquals(counter.get(), 0);
        assertTrue(counter.get() > 1);
    }

    @Test
    public void testStop() {
        final AtomicInteger counter = new AtomicInteger();
        final Scheduler scheduler = new Scheduler(new Runner(counter), TimeUnit.MILLISECONDS);

        scheduler.run(1);
        waitALittleBit();

        scheduler.stop();
        final int counterAfterStop = counter.get();
        waitALittleBit();

        assertNotEquals(counter.get(), 0);
        assertEquals(counterAfterStop, counter.get());
    }

    @Test
    public void testStopWithoutRun() {
        final AtomicInteger counter = new AtomicInteger();
        final Scheduler scheduler = new Scheduler(new Runner(counter), TimeUnit.MILLISECONDS);

        scheduler.stop();
    }

    @Test
    public void testStopAndStart() {
        final AtomicInteger counter = new AtomicInteger();
        final Scheduler scheduler = new Scheduler(new Runner(counter), TimeUnit.MILLISECONDS);

        scheduler.run(1);
        waitALittleBit();

        scheduler.stop();
        final int counterAfterStop = counter.get();
        waitALittleBit();

        scheduler.run(1);
        waitALittleBit();

        assertNotEquals(counter.get(), 0);
        assertTrue(counterAfterStop < counter.get());
    }

    private static class Runner implements Runnable {

        private final AtomicInteger counter;

        Runner(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            counter.incrementAndGet();
        }
    }

    private void waitALittleBit() {
        try {
            Thread.sleep(WAIT_A_LITTLE_BIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
