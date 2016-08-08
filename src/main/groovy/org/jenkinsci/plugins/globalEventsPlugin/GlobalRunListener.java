package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.jenkinsci.plugins.globalEventsPlugin.util.MapBuilder;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalRunListener extends RunListener<Run> {

    private static Logger log = Logger.getLogger(GlobalRunListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalRunListener() {
        log.fine(">>> Initialised");
    }

    private GlobalEventsPlugin.DescriptorImpl parentPluginDescriptorOverride = null;

    private GlobalEventsPlugin.DescriptorImpl getParentPluginDescriptor() {
        if (parentPluginDescriptorOverride != null) {
            return parentPluginDescriptorOverride;
        } else {
            return GlobalEventsPlugin.getSingletonDescriptor();
        }
    }

    @Override
    public void onDeleted(final Run run) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_DELETED, log, MapBuilder
                .put("run", run)
                .build());
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_STARTED, log, MapBuilder
                .put("run", run)
                .put("listener", listener)
                .build());
    }

    @Override
    public void onFinalized(final Run run) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_FINALIZED, log, MapBuilder
                .put("run", run)
                .build());
    }

    @Override
    public void onCompleted(final Run run, @Nonnull final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.JOB_COMPLETED, log, MapBuilder
                .put("run", run)
                .put("listener", listener)
                .build());
    }

}

