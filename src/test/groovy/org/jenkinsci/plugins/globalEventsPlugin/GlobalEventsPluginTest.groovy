package org.jenkinsci.plugins.globalEventsPlugin

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Created by nickgrealy@gmail.com.
 */
class GlobalEventsPluginTest {

    GlobalEventsPlugin.DescriptorImpl plugin
    LoggerTrap logger

    @Before
    void setup(){
        // disable load method, create new plugin...
        GlobalEventsPlugin.DescriptorImpl.metaClass.load = {}
        plugin = new GlobalEventsPlugin.DescriptorImpl()
        logger = new LoggerTrap(GlobalEventsPluginTest.name)
    }

    @Test
    void testTrappingSystemOutput(){
        plugin.safeExecGroovyCode(logger, """
println 'Foobar1'
System.out.println('Foobar2')
System.err.println('Foobar3')
log.info('Foobar4')
""", [:])
        // only using the logger captures output...
        assert logger.info == ['Foobar4']
    }

    @Test
    void testGrabbingDependencies(){
        plugin.safeExecGroovyCode(logger, """
@Grab('com.github.groovy-wslite:groovy-wslite:1.1.2')
def x = 1 // <- hack, otherwise compiler complains...
log.info(wslite.soap.SOAPClient.simpleName)
""", [:])
        assert logger.info == ['SOAPClient']
    }

    @Test
    void testPassingInputs(){
        plugin.safeExecGroovyCode(logger, """
assert aaa == 111
[success:true]
""", [aaa:111])
        assert plugin.context == [success:true]
    }

    @Test
    void testUsingLogger(){
        plugin.safeExecGroovyCode(logger, "log.info 'Foobar'", [:])
        assert logger.info == ['Foobar']
    }

    @Test
    void testSavingContext(){
        // check return values are added to context...
        plugin.context = [aaa:111]
        plugin.safeExecGroovyCode(logger, """
assert aaa == 111
[bbb:222]
""", [:])
        plugin.context == [aaa:111,bbb:222]
        // check context gets updated in following script...
        plugin.safeExecGroovyCode(logger, """
assert aaa == 111
assert bbb == 222
[ccc:333]
""", [:])
        plugin.context == [aaa:111,bbb:222,ccc:333]
    }

    @Test
    void testExceptionHandling(){
        plugin.safeExecGroovyCode(logger, "throw new RuntimeException('ERR')", [:])
        // exception should be wrapped, logged and suppressed (i.e. not bubble up to here)!
        assert logger.severe == ['>>> Caught unhandled exception!']
    }

    @Ignore("Static one time compilation has not yet been implemented!")
    @Test
    void testStaticCompilation(){
    }
}
