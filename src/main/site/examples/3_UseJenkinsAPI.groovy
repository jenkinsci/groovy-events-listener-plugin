import hudson.model.*;
import jenkins.metrics.impl.TimeInQueueAction
import org.jenkinsci.plugins.globalEventsPlugin.Event;
import hudson.model.Action;

if (event == Event.JOB_FINALIZED) {
    // Current run/build
    def build = Thread.currentThread().executable
    // Action from Metrics-plugin
    def queueAction = build.getAction(TimeInQueueAction.class)
    def queuing = queueAction.getQueuingDurationMillis()

    log.info "run_number=$build.number, run_timestamp=$build.timestamp.timeInMillis, queue_duration=$queuing"
}

if (event == Event.WORKFLOW_ACTION) {
    def flowNodeName = flowNode.getClass().toString();
    for (final Action action : flowNode.getActions()) {
        def actionName = action.getClass().toString();
        log.info "flowNode=$flowNodeName, actionName=$actionName"
    }
}
