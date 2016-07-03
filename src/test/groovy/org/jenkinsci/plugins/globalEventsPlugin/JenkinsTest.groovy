package org.jenkinsci.plugins.globalEventsPlugin

import com.gargoylesoftware.htmlunit.WebAssert
import com.gargoylesoftware.htmlunit.html.HtmlDivision
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

public class JenkinsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule()

    @Test
    public void testConfig() throws Exception {

        // open config page...
        HtmlPage configPage = j.createWebClient().goTo("configure");

        // check we have the correct page...
        WebAssert.assertTextPresent(configPage, "Groovy Events Listener Plugin");

        // set the script content...
        configPage.executeJavaScript('document.getElementById(\'gelpCode\').value = \'log.info("${Class.forName(\'org.apache.ivy.core.report.ResolveReport\').canonicalName}")\'')
//        configPage.executeJavaScript("document.getElementById('gelpCode').value = 'log.info(\"FOOBAR\")'")
//        configPage.executeJavaScript("document.getElementById('gelpCode').codemirrorObject.setValue('log.info(\"FOOBAR\")')")

        // click "Test"...
        WebAssert.assertElementNotPresentByXPath(configPage, "//div[@class='ok']")
        configPage.executeJavaScript("document.getElementsBySelector('button').filter(function(val){ return val.innerHTML == 'Test Groovy Code'; })[0].click()")

        // get the output...
        def okBlock = configPage.getByXPath("//div[@class='ok']")
        def errorBlock = configPage.getByXPath("//div[@class='error']")

        // error block should NOT be present...
        if (!errorBlock.isEmpty()){
            HtmlDivision div = (HtmlDivision) errorBlock[0]
            Assert.fail("ERROR Section: -->>" + div.asText() + "<<--")
        }
        if (!okBlock.isEmpty()){
            HtmlDivision div = (HtmlDivision) okBlock[0]
            Assert.assertEquals(
                    """
Execution completed successfully!

>>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]

FOOBAR

>>> Ignoring response - value is null or not a Map. response=null

>>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis=`X`""",
                    div.asText().replaceAll("\\d+", "X"))
        } else {
            Assert.fail("OK Section was not present!")
        }
//        def result = configPage.executeJavaScript("document.getElementsByClassName('ok')[0].innerHTML")
//        WebAssert.assertElementPresentByXPath(configPage, "//div[@class='ok']")

    }
}
