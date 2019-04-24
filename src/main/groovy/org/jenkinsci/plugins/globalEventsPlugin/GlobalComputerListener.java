package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalComputerListener extends ComputerListener {

    protected static Logger log = Logger.getLogger(GlobalComputerListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalComputerListener() {
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
    public void onLaunchFailure(final Computer computer, final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_LAUNCH_FAILURE, log, new HashMap<Object, Object>() {{
            put("computer", computer);
            put("listener", listener);
        }});
    }

    @Override
    public void onOnline(final Computer computer, final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_ONLINE, log, new HashMap<Object, Object>() {{
            put("computer", computer);
            put("listener", listener);
        }});
    }

    @Override
    public void onOffline(final Computer computer, final OfflineCause cause) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_OFFLINE, log, new HashMap<Object, Object>() {{
            put("computer", computer);
            put("cause", cause);
        }});
    }

    @Override
    public void onTemporarilyOnline(final Computer computer) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_TEMP_ONLINE, log, new HashMap<Object, Object>() {{
            put("computer", computer);
        }});
    }

    @Override
    public void onTemporarilyOffline(final Computer computer, final OfflineCause cause) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_TEMP_OFFLINE, log, new HashMap<Object, Object>() {{
            put("computer", computer);
            put("cause", cause);
        }});
    }
}

