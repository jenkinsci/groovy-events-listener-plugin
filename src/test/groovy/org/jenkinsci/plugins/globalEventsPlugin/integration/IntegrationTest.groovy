package org.jenkinsci.plugins.globalEventsPlugin.integration

import org.junit.Test

import static org.jenkinsci.plugins.globalEventsPlugin.integration.IntegrationUtils.*

/**
 * N.B. Requires a running Jenkins instance on port '8080'.
 */
class IntegrationTest {

    /*
     * The script should execute with the correct Groovy version.
     */
    @Test
    void groovyVersion() {
        def groovyCode = 'log.info GroovySystem.version'

        def expectedOutput = '''
            Execution completed successfully!
            >>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]
            2.4.8
            >>> Ignoring response - value is null or not a Map. response=null
            >>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis='X'
        '''.stripIndent()

        verifyTestGroovyCode(groovyCode, expectedOutput)
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

        verifyTestGroovyCode(groovyCode, expectedOutput)
    }
}
