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

import java.util.logging.Level
import java.util.logging.Logger

@ExportedBean
class GlobalEventsPlugin extends Plugin implements Describable<GlobalEventsPlugin> {

    private final static Logger log = Logger.getLogger(GlobalEventsPlugin.class.getName())

    @Extension
    public final static DescriptorImpl descriptor = getStaticDescriptor()

    void start() {
        getDescriptor().safeExecOnEventGroovyCode(log, [event: Event.PLUGIN_STARTED])
        log.fine(">>> Initialising ${this.class.simpleName}... [DONE]")
    }

    @Override
    void stop() {
        super.stop()
        getDescriptor().safeExecOnEventGroovyCode(log, [event: Event.PLUGIN_STOPPED])
    }

    DescriptorImpl getDescriptor() {
        getStaticDescriptor()
    }

    static DescriptorImpl getStaticDescriptor(){
        if (descriptor == null){
            try {
                return new DescriptorImpl(Jenkins.getInstance().getPluginManager().uberClassLoader)
            } catch (NullPointerException npe){
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
        private final transient GroovyClassLoader groovyClassLoader
        private transient Script groovyScript

        protected transient Map<Object, Object> context = new HashMap<Object, Object>()
        protected String onEventGroovyCode = getDefaultOnEventGroovyCode()

        /**
         * In order to load the persisted global configuration,  you have to
         * call load() in the constructor.
         */
        DescriptorImpl(ClassLoader classLoader) {
            load()
            groovyClassLoader = new GroovyClassLoader(classLoader);
            groovyScript = getScriptReadyToBeExecuted(getOnEventGroovyCode())
        }

        DescriptorImpl setContext(Map<Object, Object> context) {
            this.context = context
            return this
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
            groovyScript = getScriptReadyToBeExecuted(onEventGroovyCode)
            save() // save configuration
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
                Logger log,
                Script groovyScript,
                Map<Object, Object> params,
                boolean testMode = false) {
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

                    synchronized (groovyScript) {
                        // add all parameters from the in-memory context...
                        params.put("context", context)
                        log.finer(">>> Executing groovy script - parameters: ${params.keySet()}")

                        groovyScript.setBinding(new Binding(params))

                        executionStart = System.currentTimeMillis()
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
                log.log(Level.SEVERE, ">>> Caught unhandled exception!", t)
                if (testMode) {
                    return FormValidation.error("\nAn exception was caught.\n\n" + stringifyException(t))
                }
            }
        }

        public FormValidation doTestGroovyCode(@QueryParameter("onEventGroovyCode") final String onEventGroovyCode
        ) {
            Script script = getScriptReadyToBeExecuted(onEventGroovyCode);
            LoggerTrap logger = new LoggerTrap(GlobalEventsPlugin.name)
            def validationResult = safeExecGroovyCode(logger, script, [
                    event: Event.JOB_STARTED,
                    env  : [:],
                    run  : [:],
            ], true)
            if (validationResult == null) {
                validationResult = FormValidation.ok("\nExecution completed successfully!\n\n${logger.all.join("\n\n")}")
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



