package org.jenkinsci.plugins.globalEventsPlugin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalQueueListener extends QueueListener {

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Needs to be overridden from tests")
    protected static Logger log = Logger.getLogger(GlobalQueueListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalQueueListener() {
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
    public void onEnterWaiting(final WaitingItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_WAITING, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
    }

    @Override
    public void onEnterBlocked(final BlockedItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_BLOCKED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
    }

    @Override
    public void onEnterBuildable(final BuildableItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_BUILDABLE, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
    }

    @Override
    public void onLeft(final LeftItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_LEFT, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
    }
}

