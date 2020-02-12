package org.jenkinsci.plugins.globalEventsPlugin.integration

import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

import static org.jenkinsci.plugins.globalEventsPlugin.integration.IntegrationUtils.*

class IntegrationTest {

    @ClassRule public static JenkinsRule j = new JenkinsRule()

    @BeforeClass
    static void disableCsrfProtection() {
        j.jenkins.setCrumbIssuer(null)
    }

    /*
     * The script should execute with the correct Groovy version.
     */
    @Test
    void groovyVersion() {
        def groovyCode = 'log.info GroovySystem.version'

        def expectedOutput = '''
            Execution completed successfully!
            >>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
            2.4.12
            >>> Ignoring response - value is null or not a Map. response=null
            >>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis='X'
        '''.stripIndent()

        verifyTestGroovyCode(j.getURL(), groovyCode, expectedOutput)
    }

    /*
     * It should be possible to use the @Grab annotation to import dependencies.
     * See https://github.com/jenkinsci/groovy-events-listener-plugin/issues/21.
     */
    @Test
    void importDependenciesWithGrabAnnotation() {
        def groovyCode = '''
            @Grab('commons-lang:commons-lang:2.4')
            import org.apache.commons.lang.WordUtils
            log.info "Hello ${WordUtils.capitalize('world')}!"
        '''.stripIndent()

        def expectedOutput = '''
            Execution completed successfully!
            >>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
            Hello World!
            >>> Ignoring response - value is null or not a Map. response=null
            >>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis='X'
        '''.stripIndent()

        verifyTestGroovyCode(j.getURL(), groovyCode, expectedOutput)
    }
}
