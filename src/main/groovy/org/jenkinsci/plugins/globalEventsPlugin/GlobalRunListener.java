package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalRunListener extends RunListener<Run> {

    protected static Logger log = Logger.getLogger(GlobalRunListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalRunListener() {
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
    public void onDeleted(final Run run) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_DELETED, log, new HashMap<Object, Object>() {{
            put("run", run);
        }});
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_STARTED, log, new HashMap<Object, Object>() {{
            put("run", run);
            put("listener", listener);
        }});
    }

    @Override
    public void onFinalized(final Run run) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_FINALIZED, log, new HashMap<Object, Object>() {{
            put("run", run);
        }});
    }

    @Override
    public void onCompleted(final Run run, final @Nonnull TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_COMPLETED, log, new HashMap<Object, Object>() {{
            put("run", run);
            put("listener", listener);
        }});
    }
}

