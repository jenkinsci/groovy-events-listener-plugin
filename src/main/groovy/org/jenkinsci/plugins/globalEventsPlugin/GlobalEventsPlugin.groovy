package org.jenkinsci.plugins.globalEventsPlugin

import hudson.Extension
import hudson.Plugin
import hudson.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.util.FormValidation
import hudson.util.LogTaskListener
import jenkins.model.Jenkins
import net.sf.json.JSONObject
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest
import org.kohsuke.stapler.export.ExportedBean

import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

@ExportedBean
class GlobalEventsPlugin extends Plugin implements Describable<GlobalEventsPlugin> {
    private final static Logger log = Logger.getLogger(GlobalEventsPlugin.class.getName())

    @Extension
    public final static DescriptorImpl descriptor = getStaticDescriptor()

    private final static Scheduler scheduler = new Scheduler(
            new Runnable() {
                @Override
                public void run() {
                    descriptor.safeExecOnEventGroovyCode(log, new HashMap<Object, Object>() {
                        {
                            put("run", null);
                            put("event", Event.PLUGIN_SCHEDULE);
                        }
                    });
                }
            }, TimeUnit.MINUTES);

    void start() {
        if (descriptor.getOnPluginStarted()) {
            descriptor.safeExecOnEventGroovyCode(log, [event: Event.PLUGIN_STARTED])
        }
        log.fine(">>> Initialising ${this.class.simpleName}... [DONE]")

        final scheduleTime = descriptor.getScheduleTime();
        if (scheduleTime > 0) {
            scheduler.run(scheduleTime);
        }
    }

    @Override
    void stop() {
        super.stop()
        if (descriptor.getOnPluginStopped()) {
            getDescriptor().safeExecOnEventGroovyCode(log, [event: Event.PLUGIN_STOPPED])
        }
    }

    DescriptorImpl getDescriptor() {
        return descriptor
    }

    private static DescriptorImpl getStaticDescriptor(){
        if (descriptor == null){
            if (Jenkins.getInstance() != null) {
                return new DescriptorImpl(Jenkins.getInstance().getPluginManager().uberClassLoader)
            } else {
                // this occurs when code is run outside of a Jenkins context, use this class loader...
                return new DescriptorImpl(GlobalEventsPlugin.classLoader)
            }
        }
        return descriptor;
    }

    /**
     * Descriptor for {@link GlobalEventsPlugin}. Used as a singleton.
     * The class is marked as so that it can be accessed from views.
     */
    static final class DescriptorImpl extends Descriptor<GlobalEventsPlugin> {

        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         * <p/>
         * <p/>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private transient GroovyClassLoader groovyClassLoader
        private final transient ClassLoader parentClassLoader
        private transient Script groovyScript
        protected final transient Map<Object, Object> context = new HashMap<Object, Object>()
        protected String onEventGroovyCode = getDefaultOnEventGroovyCode()

        private boolean disableSynchronization = false;
        private boolean onPluginStarted = true;
        private boolean onPluginStopped = true;
        private boolean onJobStarted = true;
        private boolean onJobCompleted = true;
        private boolean onJobFinalized = true;
        private boolean onJobDeleted = true;
        private boolean onNodeLaunchFailure = true;
        private boolean onNodeOnline = true;
        private boolean onNodeOffline = true;
        private boolean onNodeTempOnline = true;
        private boolean onNodeTempOffline = true;
        private boolean onQueueWaiting = true;
        private boolean onQueueBlocked = true;
        private boolean onQueueBuildable = true;
        private boolean onQueueLeft = true;
        private int scheduleTime = 0;
        private String classPath = null;

        void setDisableSynchronization(boolean disableSynchronization) {
            this.disableSynchronization = disableSynchronization
        }

        boolean getOnJobDeleted() {
            return onJobDeleted
        }

        boolean getOnJobFinalized() {
            return onJobFinalized
        }

        boolean getOnJobCompleted() {
            return onJobCompleted
        }

        boolean getOnJobStarted() {
            return onJobStarted
        }

        boolean getOnNodeLaunchFailure() {
            return onNodeLaunchFailure
        }

        boolean getOnNodeOnline() {
            return onNodeOnline
        }

        boolean getOnNodeOffline() {
            return onNodeOffline
        }

        boolean getOnNodeTempOnline() {
            return onNodeTempOnline
        }

        boolean getOnNodeTempOffline() {
            return onNodeTempOffline
        }

        boolean getOnQueueWaiting() {
            return onQueueWaiting
        }

        boolean getOnQueueBlocked() {
            return onQueueBlocked
        }

        boolean getOnQueueBuildable() {
            return onQueueBuildable
        }

        boolean getOnQueueLeft() {
            return onQueueLeft
        }

        boolean getOnPluginStopped() {
            return onPluginStopped
        }

        boolean getOnPluginStarted() {
            return onPluginStarted
        }

        boolean getDisableSynchronization() {
            return disableSynchronization
        }

        int getScheduleTime() {
            return scheduleTime
        }

        @SuppressWarnings("GroovyUnusedDeclaration")
        String getClassPath() {
            return classPath
        }

        void setClassPath(String classPath) {
            this.classPath = classPath
            updateClasspath()
        }

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        DescriptorImpl(ClassLoader classLoader) {
            load()
            parentClassLoader = classLoader;
            updateClasspath()
            groovyScript = getScriptReadyToBeExecuted(getOnEventGroovyCode())
        }

        private updateClasspath() {
            groovyClassLoader = new GroovyClassLoader(parentClassLoader);

            if (classPath !=null) {
                for (String path : classPath.split(",")) {
                    groovyClassLoader.addClasspath(path.trim())
                }
            }
        }

        void putToContext(Object key, Object value) {
            context.put(key, value);
        }


        String getOnEventGroovyCode() {
            return onEventGroovyCode
        }

        void setOnEventGroovyCode(String onEventGroovyCode) {
            this.onEventGroovyCode = onEventGroovyCode
            groovyScript = getScriptReadyToBeExecuted(getOnEventGroovyCode())
        }

        String getDefaultOnEventGroovyCode() {
            return '''log.info("Fired event '${event}'.")'''
        }

        @SuppressWarnings("GroovyUnusedDeclaration")
        boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true
        }

        String getDisplayName() {
            return "groovy-events-listener-plugin"
        }

        /**
         * Can throw compilation exception.
         */
        public Script getScriptReadyToBeExecuted(String groovyCode) {
            final Class<? extends Script> clazz = groovyClassLoader.parseClass("import ${GlobalEventsPlugin.package.name}.*\n" + groovyCode);
            return clazz.newInstance();
        }

        @Override
        boolean configure(StaplerRequest req, JSONObject formData) {
            onEventGroovyCode = formData.getString("onEventGroovyCode")
            onPluginStarted = formData.getBoolean("onPluginStarted")
            onPluginStopped = formData.getBoolean("onPluginStopped")
            onJobStarted = formData.getBoolean("onJobStarted")
            onJobCompleted = formData.getBoolean("onJobCompleted")
            onJobFinalized = formData.getBoolean("onJobFinalized")
            onJobDeleted = formData.getBoolean("onJobDeleted")
            onNodeLaunchFailure = formData.getBoolean("onNodeLaunchFailure")
            onNodeOnline = formData.getBoolean("onNodeOnline")
            onNodeOffline = formData.getBoolean("onNodeOffline")
            onNodeTempOnline = formData.getBoolean("onNodeTempOnline")
            onNodeTempOffline = formData.getBoolean("onNodeTempOffline")
            onQueueWaiting = formData.getBoolean("onQueueWaiting")
            onQueueBlocked = formData.getBoolean("onQueueBlocked")
            onQueueBuildable = formData.getBoolean("onQueueBuildable")
            onQueueLeft = formData.getBoolean("onQueueLeft")
            disableSynchronization = formData.getBoolean("disableSynchronization")
            scheduleTime = formData.getInt("scheduleTime")
            classPath = formData.getString("classPath")

            updateClasspath()
            groovyScript = getScriptReadyToBeExecuted(onEventGroovyCode)

            save() // save configuration

            if (scheduleTime > 0) {
                scheduler.run(scheduleTime)
                log.finer(">>> Enable sheduler. Schedule time is " + scheduleTime)
            }
            else {
                scheduler.stop()
                log.finer(">>> Scheduler was stopped")
            }

            return super.configure(req, formData)
        }

        void safeExecOnEventGroovyCode(Logger log, Map<Object, Object> params) {
            safeExecGroovyCode(log, groovyScript, params)
        }

        /**
         * Executes groovy code!
         *
         * @param log - for some reason the parent logger doesn't log to Jenkins log.
         * @param groovyCode
         * @param params
         */
        private FormValidation safeExecGroovyCode(
                final Logger log,
                final Script groovyScript,
                final Map<Object, Object> params,
                final boolean testMode = false) {
            try {
                if (groovyScript) {
                    // get the global environment variables...
                    Map envVars = [:]
                    def jenkins = Jenkins.getInstance()
                    EnvironmentVariablesNodeProperty globalEnvVars = jenkins?.globalNodeProperties?.get(EnvironmentVariablesNodeProperty)
                    if (globalEnvVars){
                        envVars.putAll(globalEnvVars.envVars)
                    }
                    // get the Job's environment variables (if present)...
                    if (params.run instanceof Run) {
                        Run run = params.run
                        if (params.listener instanceof TaskListener) {
                            def tmp = run.getEnvironment((TaskListener) params.listener)
                            if (tmp){
                                envVars.putAll(tmp)
                            }
                        } else {
                            def tmp = run.getEnvironment(new LogTaskListener(log, Level.INFO))
                            if (tmp){
                                envVars.putAll(tmp)
                            }
                        }
                    }
                    params.put("env", envVars);
                    params.put("jenkins", jenkins)
                    params.put("log", log)

                    def syncStart = System.currentTimeMillis()
                    def executionStart

                    if (getDisableSynchronization()) {
                        executionStart = System.currentTimeMillis()
                        runScript(params, log, groovyScript)
                    }
                    else {
                        synchronized (groovyScript) {
                            executionStart = System.currentTimeMillis()
                            runScript(params, log, groovyScript)
                        }
                    }

                    def totalDurationMillis = System.currentTimeMillis() - syncStart
                    def executionDurationMillis = System.currentTimeMillis() - executionStart
                    def synchronizationMillis=totalDurationMillis-executionDurationMillis

                    log.finer(">>> Executing groovy script completed successfully. "+
                            "totalDurationMillis='$totalDurationMillis'," +
                            "executionDurationMillis='$executionDurationMillis'," +
                            "synchronizationMillis=`$synchronizationMillis`")
                } else {
                    log.warning(">>> Skipping execution, Groovy code was null or blank.")
                }
            } catch (Throwable t) {
                log.log(Level.SEVERE, ">>> Caught unhandled exception! " + t.getMessage(), t)
                if (testMode) {
                    return FormValidation.error("\nAn exception was caught.\n\n" + stringifyException(t))
                }
            }
        }

        private void runScript(final Map<Object, Object> params, final Logger log, final Script groovyScript) {
            // add all parameters from the in-memory context...
            params.put("context", context)
            log.finer(">>> Executing groovy script - parameters: ${params.keySet()}")

            groovyScript.setBinding(new Binding(params))

            def response = groovyScript.run()

            if (response instanceof Map) {
                // if response, add the values to the in-memory context...
                Map responseMap = (Map) response
                log.finer(">>> Adding keys to context: ${response.keySet()}")
                context.putAll(responseMap)
            } else {
                log.finer(">>> Ignoring response - value is null or not a Map. response=$response")
            }
        }

        public FormValidation doTestGroovyCode(@QueryParameter("onEventGroovyCode") final String onEventGroovyCode) {
            FormValidation validationResult;
            try {
                Script script = getScriptReadyToBeExecuted(onEventGroovyCode);
                LoggerTrap logger = new LoggerTrap(GlobalEventsPlugin.name)
                validationResult = safeExecGroovyCode(logger, script, [
                        event: Event.JOB_STARTED,
                        env  : [:],
                        run  : [:],
                ], true)
                if (validationResult == null) {
                    validationResult = FormValidation.ok("\nExecution completed successfully!\n\n${logger.all.join("\n\n")}")
                }
            } catch (Throwable t){
                validationResult = FormValidation.error("\nAn exception was caught.\n\n" + stringifyException(t))
            }
            validationResult
        }
    }

    /**
     * <p>Converts a Throwable stacktrace to a String.</p>
     * <p>Using ExceptionUtils causes "com.google.inject.CreationException: Guice creation errors:".</p>
     */
    private static String stringifyException(Throwable t) {
        StringWriter sw = new StringWriter()
        t.printStackTrace(new PrintWriter(sw))
        sw.toString()
    }
}
