package org.jenkinsci.plugins.globalEventsPlugin

import cucumber.api.java.Before
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import hudson.util.FormValidation

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class StepDefs {

    GlobalEventsPlugin.DescriptorImpl plugin
    LoggerTrap logger
    String groovyScript
    FormValidation validationResponse
    Throwable compilationException

    @Before
    void setup() {
        // disable load method, create new plugin...
        GlobalEventsPlugin.DescriptorImpl.metaClass.load = {}
        plugin = new GlobalEventsPlugin.DescriptorImpl(ClassLoader.getSystemClassLoader())
        logger = new LoggerTrap(GlobalEventsPluginTest.name)
    }

    @Given('^the script$')
    public void the_script(String script) {
        this.groovyScript = script
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

    @When('^the script is executed$')
    public void the_script_is_executed() {
        try {
            validationResponse = plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted(groovyScript), [:], true)
        } catch (Throwable t) {
            t.printStackTrace()
            compilationException = t
        }
    }

    @Then('^the log should display$|^the log should display \'(.+)\'$')
    public void the_log_should_display(String expectedInfoLog) {
        assert logger.info == [expectedInfoLog]
    }

    @Then('^an exception should be thrown with the message$')
    public void exception_thrown(String expectedExceptionMessage) {
        def actualMessage = compilationException.message
        assertThat(actualMessage, actualMessage.contains(expectedExceptionMessage), equalTo(true))
    }

    @Then('^the script result should be (.+) with message$')
    public void the_validation_message(String expectedValidationKind, String expectedValidationMessage) {
        assert validationResponse.kind == FormValidation.Kind.valueOf(expectedValidationKind)
        assertThat(validationResponse.message, validationResponse.message.contains(expectedValidationMessage), equalTo(true))
    }

}
