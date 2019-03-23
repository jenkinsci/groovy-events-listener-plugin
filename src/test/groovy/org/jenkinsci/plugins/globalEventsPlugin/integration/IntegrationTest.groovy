package org.jenkinsci.plugins.globalEventsPlugin.integration

import org.junit.Ignore
import org.junit.Test

import static org.jenkinsci.plugins.globalEventsPlugin.integration.IntegrationUtils.*

/**
 * N.B. Requires a running Jenkins instance on port '8080'.
 */
class IntegrationTest {

    /**
     * The script should execute with the correct Groovy version...
     */
    @Test
    public void verifyGroovyVersion() {

        verifyTestGroovyCode(
                'log.info GroovySystem.version',
                '''Execution completed successfully!
>>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
2.4.8
>>> Ignoring response - value is null or not a Map. response=null
>>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis='X\''''
        )
    }

    /**
     * We should be able to use the @Grab annotation to import dependencies...
     */
    @Test
    public void issues21() {
        // Causes -> An exception was caught.java.lang.NoClassDefFoundError: org/apache/ivy/core/report/ResolveReport
        verifyTestGroovyCode(
                '''
@Grab('commons-lang:commons-lang:2.4')
import org.apache.commons.lang.WordUtils
log.info "Hello ${WordUtils.capitalize('world')}!"
''',
                '''Execution completed successfully!
>>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
Hello World!
>>> Ignoring response - value is null or not a Map. response=null
>>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis='X\''''
        )
    }
}
