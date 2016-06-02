package org.jenkinsci.plugins.globalEventsPlugin;

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

    protected static Logger log = Logger.getLogger(GlobalQueueListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalQueueListener() {
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
    public void onEnterWaiting(final WaitingItem item) {
        if (getParentPluginDescriptor().getOnQueueWaiting()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("item", item);
                put("event", Event.QUEUE_WAITING);
            }});
        }
    }

    @Override
    public void onEnterBlocked(final BlockedItem item) {
        if (getParentPluginDescriptor().getOnQueueBlocked()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("item", item);
                put("event", Event.QUEUE_BLOCKED);
            }});
        }
    }

    @Override
    public void onEnterBuildable(final BuildableItem item) {
        if (getParentPluginDescriptor().getOnQueueBuildable()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("item", item);
                put("event", Event.QUEUE_BUILDABLE);
            }});
        }
    }

    @Override
    public void onLeft(final LeftItem item) {
        if (getParentPluginDescriptor().getOnQueueLeft()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("item", item);
                put("event", Event.QUEUE_LEFT);
            }});
        }
    }
}

