package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;
import org.jenkinsci.plugins.globalEventsPlugin.util.MapBuilder;

import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalQueueListener extends QueueListener {

    private static Logger log = Logger.getLogger(GlobalQueueListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalQueueListener() {
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
    public void onEnterWaiting(final WaitingItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_WAITING, log, MapBuilder
                .put("item", item)
                .build());
    }

    @Override
    public void onEnterBlocked(final BlockedItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_BLOCKED, log, MapBuilder
                .put("item", item)
                .build());
    }

    @Override
    public void onEnterBuildable(final BuildableItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_BUILDABLE, log, MapBuilder
                .put("item", item)
                .build());
    }

    @Override
    public void onLeft(final LeftItem item) {
        this.getParentPluginDescriptor().processEvent(Event.QUEUE_LEFT, log, MapBuilder
                .put("item", item)
                .build());
    }
}

