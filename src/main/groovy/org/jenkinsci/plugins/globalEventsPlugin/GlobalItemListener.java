package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by Renjith Pillai (i306570) on 15/09/16.
 */
@Extension
public class GlobalItemListener extends ItemListener
{
    protected static Logger log = Logger.getLogger(GlobalItemListener.class.getName());

    public GlobalItemListener() {
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
    public void onCreated(final Item item) {
        this.getParentPluginDescriptor().processEvent(Event.ITEM_CREATED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
        super.onCreated(item);
    }

    @Override
    public void onCopied(Item src, final Item item) {
        this.getParentPluginDescriptor().processEvent(Event.ITEM_COPIED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
        super.onCopied(src, item);
    }

    @Override
    public void onDeleted(final Item item) {
        this.getParentPluginDescriptor().processEvent(Event.ITEM_DELETED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
        super.onDeleted(item);
    }

    @Override
    public void onRenamed(final Item item, String oldName, String newName) {
        this.getParentPluginDescriptor().processEvent(Event.ITEM_RENAMED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
        super.onRenamed(item, oldName, newName);
    }

    @Override
    public void onLocationChanged(final Item item, String oldFullName, String newFullName) {
        this.getParentPluginDescriptor().processEvent(Event.ITEM_LOCATION_CHANGED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
        super.onLocationChanged(item, oldFullName, newFullName);
    }

    @Override
    public void onUpdated(final Item item) {
        this.getParentPluginDescriptor().processEvent(Event.ITEM_UPDATED, log, new HashMap<Object, Object>() {{
            put("item", item);
        }});
        super.onUpdated(item);
    }
}
