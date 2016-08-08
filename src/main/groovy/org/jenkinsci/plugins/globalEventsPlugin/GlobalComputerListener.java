package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;
import org.jenkinsci.plugins.globalEventsPlugin.util.MapBuilder;

import java.util.logging.Logger;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalComputerListener extends ComputerListener {

    private static Logger log = Logger.getLogger(GlobalComputerListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalComputerListener() {
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
    public void onLaunchFailure(final Computer computer, final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_LAUNCH_FAILURE, log, MapBuilder
                .put("computer", computer)
                .put("listener", listener)
                .build());
    }

    @Override
    public void onOnline(final Computer computer, final TaskListener listener) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_ONLINE, log, MapBuilder
                .put("computer", computer)
                .put("listener", listener)
                .build());
    }

    @Override
    public void onOffline(final Computer computer, final OfflineCause cause) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_OFFLINE, log, MapBuilder
                .put("computer", computer)
                .put("cause", cause)
                .build());
    }

    @Override
    public void onTemporarilyOnline(final Computer computer) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_TEMP_ONLINE, log, MapBuilder
                .put("computer", computer)
                .build());
    }

    @Override
    public void onTemporarilyOffline(final Computer computer, final OfflineCause cause) {
        this.getParentPluginDescriptor().processEvent(Event.NODE_TEMP_OFFLINE, log, MapBuilder
                .put("computer", computer)
                .put("cause", cause)
                .build());
    }

}

