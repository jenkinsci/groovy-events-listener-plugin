package org.jenkinsci.plugins.globalEventsPlugin;

import java.util.concurrent.*;

public class Scheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable runnable;
    private final TimeUnit unit;
    private ScheduledFuture<?> scheduledRun;

    public Scheduler(Runnable runnable, TimeUnit unit) {
        this.runnable = runnable;
        this.unit = unit;
    }

    public void run(int period) {
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
