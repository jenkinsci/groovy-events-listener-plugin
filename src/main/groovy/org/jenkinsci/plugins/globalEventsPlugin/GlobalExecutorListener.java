package org.jenkinsci.plugins.globalEventsPlugin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalExecutorListener implements ExecutorListener {

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Needs to be overridden from tests")
    protected static Logger log = Logger.getLogger(GlobalExecutorListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalExecutorListener() {
        log.fine(">>> Initialised");
    }

    GlobalEventsPlugin.DescriptorImpl parentPluginDescriptorOverride = null;

    GlobalEventsPlugin.DescriptorImpl getParentPluginDescriptor() {
        if (parentPluginDescriptorOverride != null) {
            return parentPluginDescriptorOverride;
        } else {
            return Jenkins.getInstance().getPlugin(GlobalEventsPlugin.class).getDescriptor();
        }
    }

    @Override
    public void taskStarted(Executor executor, Queue.Task task) {
        this.getParentPluginDescriptor().processEvent(Event.TASK_STARTED, log, new HashMap<Object, Object>() {{
            put("executor", executor);
            put("task", task);
        }});
    }

    @Override
    public void taskCompleted(Executor executor, Queue.Task task, long durationMS) {
        this.getParentPluginDescriptor().processEvent(Event.TASK_COMPLETED, log, new HashMap<Object, Object>() {{
            put("executor", executor);
            put("task", task);
            put("durationMS", durationMS);
        }});
    }

    @Override
    public void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {
        this.getParentPluginDescriptor().processEvent(Event.TASK_COMPLETED_WITH_PROBLEMS, log, new HashMap<Object, Object>() {{
            put("executor", executor);
            put("task", task);
            put("durationMS", durationMS);
            put("problems", problems);
        }});
    }
}
