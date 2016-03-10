package org.jenkinsci.plugins.globalEventsPlugin
import org.junit.Before
import org.junit.Test
/**
 * Tests that when the listener is invoked, the groovy is invoked with an "event" variable.
 * // todo test all parameters are passed into the groovy script (e.g. run, etc)
 *
 * Created by nickgrealy@gmail.com.
 */
class GlobalRunListenerTest {
    GlobalEventsPlugin.DescriptorImpl plugin
    GlobalRunListener listener

    @Before
    void setup(){
        // disable load method, create new plugin, set default groovy script...
        GlobalEventsPlugin.DescriptorImpl.metaClass.load = {}
        plugin = new GlobalEventsPlugin.DescriptorImpl(ClassLoader.getSystemClassLoader())

        // use the groovy code feature, to capture the event being triggered...
        plugin.setOnEventGroovyCode("[event: event]")
        // setup a new listener, with an overridden parent descriptor...
        listener = new GlobalRunListener()
        listener.parentPluginDescriptorOverride = plugin
    }

    @Test
    void testOnStartedEvent(){
        listener.onStarted(null, null)
        assert plugin.context == [event: Event.JOB_STARTED]
    }

    @Test
    void testOnCompletedEvent(){
        listener.onCompleted(null, null)
        assert plugin.context == [event: Event.JOB_COMPLETED]
    }

    @Test
    void testOnFinalizedEvent(){
        listener.onFinalized(null)
        assert plugin.context == [event: Event.JOB_FINALIZED]
    }

    @Test
    void testOnDeletedEvent(){
        listener.onDeleted(null)
        assert plugin.context == [event: Event.JOB_DELETED]
    }

}
