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
        if (parentPluginDescriptorOverride != null){
            return parentPluginDescriptorOverride;
        } else {
            return Jenkins.getInstance().getPlugin(GlobalEventsPlugin.class).getDescriptor();
        }
    }

    @Override
    public void onLaunchFailure(final Computer computer, final TaskListener listener) {
        if (getParentPluginDescriptor().getOnNodeLaunchFailure()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("computer", computer);
                put("listener", listener);
                put("event", Event.NODE_LAUNCH_FAILURE);
            }});
        }
    }

    @Override
    public void onOnline(final Computer computer, final TaskListener listener) {
        if (getParentPluginDescriptor().getOnNodeOnline()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("computer", computer);
                put("listener", listener);
                put("event", Event.NODE_ONLINE);
            }});
        }
    }

    @Override
    public void onOffline(final Computer computer, final OfflineCause cause) {
        if (getParentPluginDescriptor().getOnNodeOffline()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("computer", computer);
                put("cause", cause);
                put("event", Event.NODE_OFFLINE);
            }});
        }
    }

    @Override
    public void onTemporarilyOnline(final Computer computer) {
        if (getParentPluginDescriptor().getOnNodeTempOnline()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("computer", computer);
                put("event", Event.NODE_TEMP_ONLINE);
            }});
        }
    }

    @Override
    public void onTemporarilyOffline(final Computer computer, final OfflineCause cause) {
        if (getParentPluginDescriptor().getOnNodeTempOffline()) {
            this.getParentPluginDescriptor().safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {{
                put("computer", computer);
                put("cause", cause);
                put("event", Event.NODE_TEMP_OFFLINE);
            }});
        }
    }

}

