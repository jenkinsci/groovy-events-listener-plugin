package org.jenkinsci.plugins.globalEventsPlugin

import com.gargoylesoftware.htmlunit.WebAssert
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

public class JenkinsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule()

    @Test
    public void testConfig() throws Exception {
        HtmlPage configPage = j.createWebClient().goTo("configure");

        // check we have the correct page...
        WebAssert.assertTextPresent(configPage, "Groovy Events Listener Plugin");

        // set the script content...
        configPage.executeJavaScript("document.getElementById('gelpCode').codemirrorObject.setValue('log.info(\"WORKS\")')")

        // click "Test"...
        configPage.executeJavaScript("document.getElementsBySelector('button').filter(function(val){ return val.innerHTML == 'Test Groovy Code'; })[0].click()")

        // get the output...
//        def result = configPage.executeJavaScript("document.getElementsByClassName('ok')[0].innerHTML")
        WebAssert.assertElementPresentByXPath(configPage, "//div[@class='ok']")

    }
}
