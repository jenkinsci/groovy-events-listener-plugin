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
        plugin = new GlobalEventsPlugin.DescriptorImpl(ClassLoader.getSystemClassLoader())
        logger = new LoggerTrap(GlobalEventsPluginTest.name)
    }


    @Test
    void testPassingInputs(){
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("""
            assert aaa == 111
            [success:true]
            """), [aaa:111])
        assert plugin.context == [success:true]
    }


    @Test
    void testSavingContext(){
        // check return values are added to context...
        plugin.context = [aaa:111]
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("""
            assert context.aaa == 111
            [bbb:222]
            """), [:])
        assert plugin.context == [aaa:111,bbb:222]
        // check context gets updated in following script...
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("""
            assert context.aaa == 111
            assert context.bbb == 222
            [ccc:333]
            """), [:])
        assert plugin.context == [aaa:111,bbb:222,ccc:333]
    }

    @Test
    void testExceptionHandling(){
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("throw new RuntimeException('ERR')"), [:])
        // exception should be wrapped, logged and suppressed (i.e. not bubble up to here)!
        assert logger.severe == ['>>> Caught unhandled exception!']
    }

    @Ignore("Static one time compilation has not yet been implemented!")
    @Test
    void testStaticCompilation(){
    }
}
