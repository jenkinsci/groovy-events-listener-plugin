package org.jenkinsci.plugins.globalEventsPlugin;

import jenkins.model.Jenkins;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
public class GlobalWorkflowListener implements GraphListener {

    protected static Logger log = Logger.getLogger(GlobalWorkflowListener.class.getName());
    private final Run run;

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalWorkflowListener(Run r) {
        this.run = r;
        log.fine(">>> Initialised");
    }

    GlobalEventsPlugin.DescriptorImpl parentPluginDescriptorOverride = null;

    GlobalEventsPlugin.DescriptorImpl getParentPluginDescriptor() {
        if (parentPluginDescriptorOverride != null){
            return parentPluginDescriptorOverride;
        } else {
            return Jenkins.getInstance().getPlugin(GlobalEventsPlugin.class).getDescriptor();
        }
    }

    @Override
    public void onNewHead(final FlowNode node) {
       this.getParentPluginDescriptor().processEvent(Event.WORKFLOW_NEW_HEAD, log, new HashMap<Object, Object>() {{
            put("flowNode", node);
            put("run", run);
        }});
    }
}

