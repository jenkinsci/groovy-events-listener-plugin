package org.jenkinsci.plugins.globalEventsPlugin

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

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
    void testTrappingSystemOutput(){
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("""
            println 'Foobar1';
            System.out.println('Foobar2');
            System.err.println('Foobar3');
            log.info('Foobar4');
            """), [:])
        // only using the logger captures output...
        assert logger.info == ['Foobar4']
    }

    @Test
    void testGrabbingDependencies(){
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("""
            @Grab('com.github.groovy-wslite:groovy-wslite:1.1.2')
            def x = 1 // <- hack, otherwise compiler complains...
            log.info(wslite.soap.SOAPClient.simpleName)
            """), [:])
        assert logger.info == ['SOAPClient']
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
    void testUsingLogger(){
        plugin.safeExecGroovyCode(logger, plugin.getScriptReadyToBeExecuted("log.info 'Foobar'"), [:])
        assert logger.info == ['Foobar']
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
    void testCounter(){
        int expectedValue = 1000
        plugin.context = [total:0]
        plugin.setOnEventGroovyCode("context.total += 1")
        for(int i=0; i<expectedValue; i++) {
            plugin.safeExecOnEventGroovyCode(logger, [:])
            assert plugin.context == [total: i+1]
        }
        assert plugin.context == [total:expectedValue]
    }

    @Test
    void testConcurrentCounter(){
        int expectedValue = 1000
        plugin.context = [total:0]
        plugin.setOnEventGroovyCode("context.total += 1")
        Callable<Integer> callable = new Callable() {
            @Override
            Integer call() throws Exception {
                for(int i=0; i<expectedValue/5; i++) {
                    plugin.safeExecOnEventGroovyCode(logger, [:])
                }
                return 0;
            };
        };

        ExecutorService executors = Executors.newFixedThreadPool(5);
        FutureTask task1 = new FutureTask(callable);
        FutureTask task2 = new FutureTask(callable);
        FutureTask task3 = new FutureTask(callable);
        FutureTask task4 = new FutureTask(callable);
        FutureTask task5 = new FutureTask(callable);
        executors.execute(task1);
        executors.execute(task2);
        executors.execute(task3);
        executors.execute(task4);
        executors.execute(task5);

        while (true) {
            if (task1.isDone() && task2.isDone() && task3.isDone() && task4.isDone() && task5.isDone() ) {
                break;
            }

            Thread.sleep(1000);
        }

        assert plugin.context == [total:expectedValue]
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
