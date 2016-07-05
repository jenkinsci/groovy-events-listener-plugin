package org.jenkinsci.plugins.globalEventsPlugin

import org.junit.Test

/**
 * Created by nickpersonal on 5/07/2016.
 */
class QuickTest {

    @Test
    public void testLoading(){
        Class clazz = new GroovyClassLoader(this.class.classLoader).parseClass("""
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
def test = 123
return "FOOBAR"
""")
        Script script = clazz.newInstance()
        def result = script.run()
        assert result == "FOOBAR"
    }
}
