package org.jenkinsci.plugins.globalEventsPlugin.integration

import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class IntegrationUtils {

    /**
     * Invokes the "testGroovyCode" REST service.
     */
    public static void verifyTestGroovyCode(URL jenkinsUrl, String groovyCodeInput, String expectedOutput) {
        def client = new RESTClient(jenkinsUrl)

        // don't throw exceptions for >=400 status codes...
        client.handler.failure = client.handler.success

        // wait up to 1 minute for the jenkins server to start...
        waitUntil("Server is UP", 60, { client.get(path: '').status == 200 })

        // call testGroovyCode service...
        def resp = client.post(
                path: 'descriptorByName/org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPlugin/testGroovyCode',
                body: [onEventGroovyCode: groovyCodeInput],
                requestContentType: ContentType.URLENC)

        // verify the response was success (with expected output)...
        assert resp.status == 200
        NodeChild text = resp.data

        def tmpActual = normaliseNewlines(text.text().replaceAll("'\\d+'", "'X'"))
        def tmpExpected = normaliseNewlines(expectedOutput)
        assert tmpExpected == tmpActual
    }

    private static String normaliseNewlines(String message) {
        message.replaceAll("\\r?\\n", "").replaceAll("\\s{2,}", "")
    }

    /**
     * Waits until the given condition is true.
     */
    private static void waitUntil(String conditionName, int secondsTimeout, Closure conditionIsTrue) {
        int i
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
