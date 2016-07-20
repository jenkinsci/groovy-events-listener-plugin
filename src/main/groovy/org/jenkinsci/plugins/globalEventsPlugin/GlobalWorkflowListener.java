package org.jenkinsci.plugins.globalEventsPlugin;

import jenkins.model.Jenkins;
import hudson.model.Run;
import hudson.model.Action;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jenkinsci.plugins.workflow.support.steps.StageStepExecution;
//import org.jenkinsci.plugins.workflow.cps.steps.*; //ParallelStepExecution;
//import org.jenkinsci.plugins.workflow.steps.*;

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
        System.err.println("Starting run for " + node.getId());
        for (final Action a : node.getActions()) {
            String className = a.getClass().toString();
                    /* ParallelStepExecution is not a public class, therefore instabceof does not work */
            if (   className.equals("class org.jenkinsci.plugins.workflow.cps.steps.ParallelStepExecution$ParallelLabelAction")
                || (a instanceof org.jenkinsci.plugins.workflow.support.steps.StageStepExecution) ) {
                this.getParentPluginDescriptor().processEvent(Event.WORKFLOW_ACTION, log, new HashMap<Object, Object>() {{
                    put("action", a);
                    put("flowNode", node);
                    put("run", run);
                }});
            }
        }
    }
}

