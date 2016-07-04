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

    @Test
    public void testFoobar(){
        def client = new RESTClient('http://localhost:19091/' )

        def resp = client.post(
                path: '/descriptorByName/org.jenkinsci.plugins.globalEventsPlugin.GlobalEventsPlugin/testGroovyCode',
                body: [ onEventGroovyCode: 'log.info("FOOBAR")' ],
                requestContentType: ContentType.URLENC )

        assert resp.status == 200
        NodeChild text = resp.data
        assert text.text().replaceAll("\\d+", "X") == '''Execution completed successfully!
>>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
FOOBAR
>>> Ignoring response - value is null or not a Map. response=null
>>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis=`X`'''.replaceAll("\\r?\\n", "")
    }

    public static void waitUntil(Closure isTrue, int seconds){
        for (int i = 0; i < seconds; i++){

        }
    }

}
