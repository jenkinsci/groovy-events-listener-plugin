package org.jenkinsci.plugins.globalEventsPlugin.acceptance

import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import hudson.util.FormValidation
import org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPlugin
import org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPluginTest
import org.jenkinsci.plugins.globalEventsPlugin.GlobalItemListener
import org.jenkinsci.plugins.globalEventsPlugin.GlobalRunListener
import org.jenkinsci.plugins.globalEventsPlugin.GlobalComputerListener
import org.jenkinsci.plugins.globalEventsPlugin.GlobalQueueListener
import org.jenkinsci.plugins.globalEventsPlugin.LoggerTrap

class StepDefs {

    GlobalEventsPlugin.DescriptorImpl plugin
    GlobalRunListener runListener
    GlobalComputerListener computerListener
    GlobalQueueListener queueListener
    GlobalItemListener itemListener
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

        queueListener = new GlobalQueueListener()
        queueListener.parentPluginDescriptorOverride = plugin
        queueListener.log = logger

        itemListener = new GlobalItemListener()
        itemListener.parentPluginDescriptorOverride = plugin
        itemListener.log = logger
    }

    @Given('^the script$')
    void the_script(String script) {
        this.groovyScript = script
        plugin.setOnEventGroovyCode(script)
    }

    @Given('^the script with exception$')
    void the_script_with_exception(String script) {
        try {
            this.groovyScript = script
            plugin.setOnEventGroovyCode(script)
        } catch (Throwable t) {
            t.printStackTrace()
            compilationException = t
        }
    }

    @When('^I test the script$')
    void i_test_the_script() {
        try {
            validationResponse = plugin.doTestGroovyCode(groovyScript)
        } catch (Throwable t) {
            t.printStackTrace()
            compilationException = t
        }
    }

    @When('^the (.+) event is triggered$')
    void the_event_is_triggered(String method) {
        try {
            switch (method) {
                case "Run.onStarted":
                    runListener.onStarted(null, null)
                    break
                case "Run.onCompleted":
                    runListener.onCompleted(null, null)
                    break
                case "Run.onFinalized":
                    runListener.onFinalized(null)
                    break
                case "Run.onDeleted":
                    runListener.onDeleted(null)
                    break
                case "Computer.onLaunchFailure":
                    computerListener.onLaunchFailure(null, null)
                    break
                case "Computer.onOnline":
                    computerListener.onOnline(null, null)
                    break
                case "Computer.onOffline":
                    computerListener.onOffline(null, null)
                    break
                case "Computer.onTemporarilyOnline":
                    computerListener.onTemporarilyOnline(null)
                    break
                case "Computer.onTemporarilyOffline":
                    computerListener.onTemporarilyOffline(null, null)
                    break
                case "Queue.onEnterWaiting":
                    queueListener.onEnterWaiting(null)
                    break
                case "Queue.onEnterBlocked":
                    queueListener.onEnterBlocked(null)
                    break
                case "Queue.onEnterBuildable":
                    queueListener.onEnterBuildable(null)
                    break
                case "Queue.onLeft":
                    queueListener.onLeft(null)
                    break
                case "Item.onUpdated":
                    itemListener.onUpdated(null)
                    break
                case "Item.onLocationChanged":
                    itemListener.onLocationChanged(null, null, null)
                    break
                case "Item.onRenamed":
                    itemListener.onRenamed(null, null, null)
                    break
                case "Item.onDeleted":
                    itemListener.onDeleted(null)
                    break
                case "Item.onCopied":
                    itemListener.onCopied(null, null)
                    break
                case "Item.onCreated":
                    itemListener.onCreated(null)
                    break
            }
        } catch (Throwable t) {
            t.printStackTrace()
            runtimeException = t
        }
    }

    @Then('^the log level (.+) should display \'(.+)\'$')
    void the_log_should_display(String logLevel, String expectedLog) {
        def actualLogInfo = logger.properties[logLevel].join("\n")
        assert actualLogInfo.contains(expectedLog)
    }

    @Then('^the log should display$')
    void the_log_should_display2(String expectedLog) {
        assert logger.all == expectedLog.readLines()
    }

    @Then('^the context should contain (.+) = (.+)$')
    void the_cache_should_contain(String key, String expectedValue) {
        def actualValue = plugin.context[key]
        assert actualValue == (expectedValue == 'null' ? null : expectedValue)
    }

    @Then('^no exception should be thrown$')
    void no_exception(){
        assert compilationException == null
        assert runtimeException == null
    }

    @Then('^an exception should be thrown with the message \'(.+)\'$')
    void exception_thrown(String expectedExceptionMessage) {
        def actualMessage = compilationException.message
        assert actualMessage.contains(expectedExceptionMessage)
    }

    @Then('^the validation result should be (.+) with message \'(.+)\'$')
    void the_validation_message(String expectedValidationKind, String expectedValidationMessage) {
        def actualKind = validationResponse.kind
        def expectedKind = FormValidation.Kind.valueOf(expectedValidationKind)
        assert actualKind == expectedKind

        def actualMessage = validationResponse.message
        assert actualMessage.contains(expectedValidationMessage)
    }
}
