package org.jenkinsci.plugins.globalEventsPlugin;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import java.util.concurrent.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class GlobalRunListener extends RunListener<Run> {

    protected static Logger log = Logger.getLogger(GlobalRunListener.class.getName());
    private ExecutorService executor = new ThreadPoolExecutor(0, 5, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    /**
     * This class is lazy loaded (as required).
     */
    public GlobalRunListener() {
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

    private void addWorkflowListener(final Run run, final TaskListener listener) {
        if (this.getParentPluginDescriptor().isEventEnabled(Event.WORKFLOW_ACTION).booleanValue()) {
            ListenableFuture<FlowExecution> promise = ((WorkflowRun) run).getExecutionPromise();
            promise.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        FlowExecution ex = ((WorkflowRun) run).getExecutionPromise().get();
                        ex.addListener(new GlobalWorkflowListener(run));
                    /*
                    * Preferably use catch (InterruptedException | ExecutionException e),
                    * but requires -source 1.7 flag.
                    */
                    } catch (InterruptedException e){
                        e.printStackTrace();
                        listener.error("Not able to get Workflow listener for this job");
                    } catch (ExecutionException  e){
                        e.printStackTrace();
                        listener.error("Not able to get Workflow listener for this job");
                    }
                }
            }, executor);
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
        if (run instanceof WorkflowRun) {
            addWorkflowListener(run, listener);
        }
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

