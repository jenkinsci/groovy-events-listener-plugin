import hudson.model.*;
import jenkins.metrics.impl.TimeInQueueAction;

if (event == 'RunListener.onFinalized') {
    // Current run/build
    def build = Thread.currentThread().executable
    // Action from Metrics-plugin
    def queueAction = build.getAction(TimeInQueueAction.class)
    def queuing = queueAction.getQueuingDurationMillis()

    log.info "run_number=$build.number, run_timestamp=$build.timestamp.timeInMillis, queue_duration=$queuing"
}