package org.jenkinsci.plugins.globalEventsPlugin

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by nickgrealy@gmail.com.
 */
class GlobalEventsPluginTest {

    private final static Logger logger = Logger.getLogger(GlobalEventsPluginTest.name)

    GlobalEventsPlugin.DescriptorImpl plugin
    List<String> loginfo
    List<String> logsevere

    @Before
    void setup(){
        // disable load method, create new plugin...
        GlobalEventsPlugin.DescriptorImpl.metaClass.load = {}
        plugin = new GlobalEventsPlugin.DescriptorImpl()
        // clear log buffer, override logger.info method...
        loginfo = []
        logsevere = []
        logger.metaClass.info = { String msg -> loginfo.add(msg) }
//        logger.metaClass.log = { Level level, String msg, Throwable t -> logsevere.add(msg) }
    }

    @Test
    void testTrappingSystemOutput(){
        plugin.safeExecGroovyCode(logger, """
println 'Foobar1'
System.out.println('Foobar2')
System.err.println('Foobar3')
""", [:])
        assert loginfo == ['Foobar1','Foobar2','Foobar3']
    }

    @Test
    void testGrabbingDependencies(){
        plugin.safeExecGroovyCode(logger, """
@Grab('com.github.groovy-wslite:groovy-wslite:1.1.2')
def x = 1 // <- hack, otherwise compiler complains...
log.info(wslite.soap.SOAPClient.simpleName)
""", [:])
        assert loginfo == ['SOAPClient']
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
        assert loginfo == ['Foobar']
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
        assert logsevere[0] == 'Caught unhandled exception!'
    }

    @Ignore("Static one time compilation has not yet been implemented!")
    @Test
    void testStaticCompilation(){
    }
}
