import hudson.model.Action
import org.jenkinsci.plugins.globalEventsPlugin.Event

if (event == Event.WORKFLOW_NEW_HEAD) {
    def flowNodeName = flowNode.getClass().toString();
    for (final Action action : flowNode.getActions()) {
        def actionName = action.getClass().toString();
        log.info "flowNode=$flowNodeName, actionName=$actionName"
    }
}
