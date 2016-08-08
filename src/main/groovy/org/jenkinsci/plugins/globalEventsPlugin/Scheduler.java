package org.jenkinsci.plugins.globalEventsPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable runnable;
    private final TimeUnit unit;
    private ScheduledFuture<?> scheduledRun;

    public Scheduler(final Runnable runnable, final TimeUnit unit) {
        this.runnable = runnable;
        this.unit = unit;
    }

    public void run(final int period) {
        if (scheduledRun != null) {
            scheduledRun.cancel(false);
        }
        scheduledRun = scheduler.scheduleAtFixedRate(runnable, 0, period, unit);
    }

    public void stop() {
        if (scheduledRun != null) {
            scheduledRun.cancel(false);
        }
    }
}
