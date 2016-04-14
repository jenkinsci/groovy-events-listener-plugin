package org.jenkinsci.plugins.globalEventsPlugin;

/**
 * Contains constants for all available event names.
 */
public final class Event {
    private Event(){
    }

    public static final String PLUGIN_STARTED = "GlobalEventsPlugin.start";
    public static final String PLUGIN_STOPPED = "GlobalEventsPlugin.stop";
    public static final String PLUGIN_SCHEDULE = "GlobalEventsPlugin.schedule";

    public static final String JOB_DELETED = "RunListener.onDeleted";
    public static final String JOB_STARTED = "RunListener.onStarted";
    public static final String JOB_FINALIZED = "RunListener.onFinalized";
    public static final String JOB_COMPLETED = "RunListener.onCompleted";

}
