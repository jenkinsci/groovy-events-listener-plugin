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

    public static final String NODE_LAUNCH_FAILURE = "ComputerListener.onLaunchFailure";
    public static final String NODE_ONLINE = "ComputerListener.onOnline";
    public static final String NODE_OFFLINE = "ComputerListener.onOffline";
    public static final String NODE_TEMP_ONLINE = "ComputerListener.onTemporarilyOnline";
    public static final String NODE_TEMP_OFFLINE = "ComputerListener.onTemporarilyOffline";

    public static final String QUEUE_WAITING = "QueueListener.onEnterWaiting";
    public static final String QUEUE_BLOCKED = "QueueListener.onEnterBlocked";
    public static final String QUEUE_BUILDABLE = "QueueListener.onEnterBuildable";
    public static final String QUEUE_LEFT = "QueueListener.onLeft";

    public static String[] getAll() {
        return new String[] {
            PLUGIN_STARTED,
            PLUGIN_STOPPED,
            PLUGIN_SCHEDULE,
            JOB_DELETED,
            JOB_STARTED,
            JOB_FINALIZED,
            JOB_COMPLETED,
            NODE_LAUNCH_FAILURE,
            NODE_ONLINE,
            NODE_OFFLINE,
            NODE_TEMP_ONLINE,
            NODE_TEMP_OFFLINE,
            QUEUE_WAITING,
            QUEUE_BLOCKED,
            QUEUE_BUILDABLE,
            QUEUE_LEFT,
        };
    }
}
