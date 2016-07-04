package org.jenkinsci.plugins.globalEventsPlugin.integration

import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.junit.Assert
import org.junit.Test

/**
 * Created by nickpersonal on 5/07/2016.
 */
class IntegrationTest {

    /**
     * N.B. Requires a running Jenkins instance on port '19091'.
     */
    @Test
    public void issues21() {

        String jenkinsUrl = 'http://localhost:19091/'

        // Causes -> An exception was caught.java.lang.NoClassDefFoundError: org/apache/ivy/core/report/ResolveReport
        String groovyCodeInput = '''
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
def test = 123
log.info("FOOBAR")
'''

        String expectedOutput = '''Execution completed successfully!
>>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
FOOBAR
>>> Ignoring response - value is null or not a Map. response=null
>>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis=`X`'''

        invokeTestGroovyCode(jenkinsUrl, groovyCodeInput, expectedOutput)
    }

    /**
     * Invokes the "testGroovyCode" REST service.
     */
    public static void invokeTestGroovyCode(String jenkinsUrl, String groovyCodeInput, String expectedOutput) {
        def client = new RESTClient(jenkinsUrl)

        // don't throw exceptions for >=400 status codes...
        client.handler.failure = client.handler.success

        // wait up to 1 minute for the jenkins server to start...
        waitUntil("Server is UP", 60, { client.get(path: '/').status == 200 })

        // call testGroovyCode service...
        def resp = client.post(
                path: '/descriptorByName/org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPlugin/testGroovyCode',
                body: [onEventGroovyCode: groovyCodeInput],
                requestContentType: ContentType.URLENC)

        // verify the response was success (with expected output)...
        assert resp.status == 200
        NodeChild text = resp.data
        assert text.text().replaceAll("\\d+", "X") == expectedOutput.replaceAll("\\r?\\n", "")
    }

    /**
     * Waits until the given condition is true.
     */
    public static void waitUntil(String conditionName, int secondsTimeout, Closure conditionIsTrue) {
        int i;
        for (i = 0; i < secondsTimeout && !conditionIsTrue(); i++) {
            println "Condition '$conditionName' not (yet) met, sleeping for 1 second..."
            Thread.sleep(1000)
        }
        if (i == secondsTimeout) {
            throw new RuntimeException("Timeout occurred after $secondsTimeout seconds, waiting for condition '$conditionName'.")
        } else {
            println "Condition '$conditionName' was met."
        }
    }

}
