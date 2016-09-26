package org.jenkinsci.plugins.globalEventsPlugin

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import net.sf.json.JSONObject

import static groovy.test.GroovyAssert.shouldFail

/**
 * Created by nickgrealy@gmail.com.
 */
class GlobalEventsPluginTest {
    private GlobalEventsPlugin.DescriptorImpl plugin
    private LoggerTrap logger

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Before
    void setup(){
        // disable load method, create new plugin...
        GlobalEventsPlugin.DescriptorImpl.metaClass.load = {}
        plugin = new GlobalEventsPlugin.DescriptorImpl(ClassLoader.getSystemClassLoader())
        logger = new LoggerTrap(GlobalEventsPluginTest.name)
    }

    @Test
    void testPassingInputs(){
        plugin.safeExecGroovyCode("dummy_event", logger, plugin.getScriptReadyToBeExecuted("""
            assert aaa == 111
            [success:true]
            """), [aaa:111])
        assert plugin.context == [success:true]
    }

    @Test
    void testImportFromTwoNewClasses(){
        File folder1 = folder.newFolder()
        PrintWriter writer = new PrintWriter(folder1.absolutePath + "/Class1.groovy", "UTF-8");
        writer.println("public class Class1 { public static int A = 1; }");
        writer.close()

        File folder2 = folder.newFolder()
        writer = new PrintWriter(folder2.absolutePath + "/Class2.groovy", "UTF-8");
        writer.println("public class Class2 { public static int A = 2; }");
        writer.close()

        writer = new PrintWriter(folder2.absolutePath + "/Class3.groovy", "UTF-8");
        writer.println("public class Class3 { public static int A = 3; }");
        writer.close()

        plugin.setClassPath(folder1.absolutePath + "  ,  " + folder2.absolutePath);

        plugin.setOnEventGroovyCode("import Class1;" +
                "import Class2;" +
                "import Class3;" +
                "context.c1 = Class1.A;" +
                "context.c2 = Class2.A;" +
                "context.c3 = Class3.A")
        plugin.processEvent("dummy_event", logger, [:])
        assert plugin.context == [c1: 1, c2: 2, c3: 3]
    }

    @Test
    void testFailToImport(){
        shouldFail MultipleCompilationErrorsException, {
            plugin.setOnEventGroovyCode("import test.TestClass")
        }
    }

    @Test
    void testCounter(){
        int expectedValue = 1000
        plugin.putToContext("total", 0)
        plugin.setOnEventGroovyCode("context.total += 1")
        for(int i=0; i<expectedValue; i++) {
            plugin.processEvent("dummy_event", logger, [:])
            assert plugin.context == [total: i+1]
        }
        assert plugin.context == [total:expectedValue]
    }

    @Test
    void testConcurrentCounter(){
        int expectedValue = 1000
        plugin.putToContext("total", 0)
        plugin.setDisableSynchronization(false)
        plugin.setOnEventGroovyCode("context.total += 1;")

        ExecutorService executors = Executors.newFixedThreadPool(5);
        FutureTask task1 = new FutureTask(callCodeMultipleTimes(expectedValue/5 as int));
        FutureTask task2 = new FutureTask(callCodeMultipleTimes(expectedValue/5 as int));
        FutureTask task3 = new FutureTask(callCodeMultipleTimes(expectedValue/5 as int));
        FutureTask task4 = new FutureTask(callCodeMultipleTimes(expectedValue/5 as int));
        FutureTask task5 = new FutureTask(callCodeMultipleTimes(expectedValue/5 as int));
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
    void testDisableSynchronizationCounter(){
        int expectedValue = 10000
        plugin.putToContext("total", 0)
        plugin.setDisableSynchronization(true)
        plugin.setOnEventGroovyCode("context.total += 1;")
        Callable<Integer> callable = callCodeMultipleTimes(expectedValue/2 as int)

        ExecutorService executors = Executors.newFixedThreadPool(2);
        FutureTask task1 = new FutureTask(callable);
        FutureTask task2 = new FutureTask(callable);
        executors.execute(task1);
        executors.execute(task2);

        while (true) {
            if (task1.isDone() && task2.isDone()) {
                break;
            }

            Thread.sleep(1000);
        }

        assert plugin.context != [total:expectedValue]
    }

    @Test
    void testEventsEnabledDefault() {
        assert plugin.isEventEnabled("nonexistent_event")
    }

    @Test
    void testUpdateConfigPluginStart() {
        JSONObject formData = getDefaultConfig()

        assert plugin.isEventEnabled("GlobalEventsPlugin.start")

        formData.put("GlobalEventsPlugin__start", false)

        plugin.update(formData)
        assert !plugin.isEventEnabled("GlobalEventsPlugin.start")

        formData.put("GlobalEventsPlugin__start", true)
        plugin.update(formData)
        assert plugin.isEventEnabled("GlobalEventsPlugin.start")
    }

    @Test
    void testUpdateConfigScheduler() {
        JSONObject formData = getDefaultConfig()

        assert plugin.isEventEnabled("GlobalEventsPlugin.schedule")

        formData.put("scheduleTime", 0)
        plugin.update(formData)
        assert !plugin.isEventEnabled("GlobalEventsPlugin.schedule")

        formData.put("scheduleTime", 1)
        plugin.update(formData)
        assert plugin.isEventEnabled("GlobalEventsPlugin.schedule")
    }

    private static JSONObject getDefaultConfig() {
        new JSONObject([
                "onEventGroovyCode"     : "",
                "disableSynchronization": false,
                "scheduleTime"          : 0,
                "classPath"             : "",
        ])
    }

    private Callable<Integer> callCodeMultipleTimes(int number) {
        return new Callable() {
            @Override
            Integer call() throws Exception {
                for(int i=0; i<number; i++) {
                    plugin.processEvent("dummy_event", logger, [:])
                }
                return 0;
            };
        };
    }
}
