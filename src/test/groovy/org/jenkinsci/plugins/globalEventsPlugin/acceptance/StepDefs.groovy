package org.jenkinsci.plugins.globalEventsPlugin.acceptance

import cucumber.api.java.Before
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import hudson.util.FormValidation
import org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPlugin
import org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPluginTest
import org.jenkinsci.plugins.globalEventsPlugin.GlobalRunListener
import org.jenkinsci.plugins.globalEventsPlugin.GlobalComputerListener
import org.jenkinsci.plugins.globalEventsPlugin.LoggerTrap

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class StepDefs {

    GlobalEventsPlugin.DescriptorImpl plugin
    GlobalRunListener runListener
    GlobalComputerListener computerListener
    LoggerTrap logger
    String groovyScript
    FormValidation validationResponse
    Throwable compilationException
    Throwable runtimeException

    @Before
    void setup() {
        // disable load method, create new plugin...
        GlobalEventsPlugin.DescriptorImpl.metaClass.load = {}
        plugin = new GlobalEventsPlugin.DescriptorImpl(ClassLoader.getSystemClassLoader())
        logger = new LoggerTrap(GlobalEventsPluginTest.name)

        // setup new listeners, with an overridden parent descriptor and logger...
        runListener = new GlobalRunListener()
        runListener.parentPluginDescriptorOverride = plugin
        runListener.log = logger
        computerListener = new GlobalComputerListener()
        computerListener.parentPluginDescriptorOverride = plugin
        computerListener.log = logger

    }

    @Given('^the script$')
    public void the_script(String script) {
        this.groovyScript = script
        plugin.setOnEventGroovyCode(script)
    }

    @Given('^the script with exception$')
    public void the_script_with_exception(String script) {
        try {
            this.groovyScript = script
            plugin.setOnEventGroovyCode(script)
        } catch (Throwable t) {
            t.printStackTrace()
            compilationException = t
        }
    }

    @When('^I test the script$')
    public void i_test_the_script() {
        try {
            validationResponse = plugin.doTestGroovyCode(groovyScript)
        } catch (Throwable t) {
            t.printStackTrace()
            compilationException = t
        }
    }

    @When('^the (.+) event is triggered$')
    public void the_event_is_triggered(String method) {
        try {
            switch (method){
                case "Run.onStarted":
                    runListener.onStarted(null, null)
                    break;
                case "Run.onCompleted":
                    runListener.onCompleted(null, null)
                    break;
                case "Run.onFinalized":
                    runListener.onFinalized(null)
                    break;
                case "Run.onDeleted":
                    runListener.onDeleted(null)
                    break;
                case "Computer.onLaunchFailure":
                    computerListener.onLaunchFailure(null, null)
                    break;
                case "Computer.onOnline":
                    computerListener.onOnline(null, null)
                    break;
                case "Computer.onOffline":
                    computerListener.onOffline(null, null)
                    break;
                case "Computer.onTemporarilyOnline":
                    computerListener.onTemporarilyOnline(null)
                    break;
                case "Computer.onTemporarilyOffline":
                    computerListener.onTemporarilyOffline(null, null)
                    break;
            }
        } catch (Throwable t){
            t.printStackTrace()
            runtimeException = t
        }
    }

    @Then('^the log level (.+) should display \'(.+)\'$')
    public void the_log_should_display(String logLevel, String expectedLog) {
        def actualLogInfo = logger.properties[logLevel].join("\n")
        assert actualLogInfo.contains(expectedLog)
    }

    @Then('^the log should display$')
    public void the_log_should_display2(String expectedLog) {
        assert logger.all == expectedLog.readLines()
    }

    @Then('^the context should contain (.+) = (.+)$')
    public void the_cache_should_contain(String key, String expectedValue) {
        def actualValue = plugin.context[key]
        assert actualValue == (expectedValue == 'null' ? null : expectedValue)
    }

    @Then('^no exception should be thrown$')
    public void no_exception(){
        assert compilationException == null
        assert runtimeException == null
    }

    @Then('^an exception should be thrown with the message \'(.+)\'$')
    public void exception_thrown(String expectedExceptionMessage) {
        def actualMessage = compilationException.message
        assertThat(actualMessage, actualMessage.contains(expectedExceptionMessage), equalTo(true))
    }

    @Then('^the validation result should be (.+) with message \'(.+)\'$')
    public void the_validation_message(String expectedValidationKind, String expectedValidationMessage) {
        assertThat(validationResponse.kind, is(FormValidation.Kind.valueOf(expectedValidationKind)))
        def message = validationResponse.message
        assertThat(message, message.contains(expectedValidationMessage), equalTo(true))
    }

}
