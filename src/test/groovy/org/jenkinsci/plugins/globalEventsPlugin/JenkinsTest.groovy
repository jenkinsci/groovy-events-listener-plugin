package org.jenkinsci.plugins.globalEventsPlugin

import com.gargoylesoftware.htmlunit.WebAssert
import com.gargoylesoftware.htmlunit.html.HtmlDivision
import com.gargoylesoftware.htmlunit.html.HtmlPage
import hudson.tools.ToolProperty
import org.junit.Assert
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

@Ignore
public class JenkinsTest {

    static {
        def jdk = System.getenv("JDK8_HOME")
        println "\n\n---->>>> Using JDK: $jdk"
        System.setProperty("java.home", jdk)
    }

    @Rule
    public JenkinsRule j = new JenkinsRule()

    @Test
    public void testConfig() throws Exception {

//        WebDriver driver = new FirefoxDriver(new FirefoxProfile(new File(System.getProperty("java.io.tmpdir"), "selenium")))
//        try {
//            def url = j.getURL().toString() + "/configure"
//            driver.get(url)
//            WebDriverWait wait = new WebDriverWait(driver, 30)
//            wait.until(ExpectedConditions.presenceOfElementLocated(By.id('gelpCode')))
//
//            Thread.sleep(20000);
//        } finally {
//            driver.close()
//        }

        // open config page...
        HtmlPage configPage = j.createWebClient().goTo("configure");

        // check we have the correct page...
        WebAssert.assertTextPresent(configPage, "Groovy Events Listener Plugin");

        // set the script content...
//        configPage.executeJavaScript('document.getElementById(\'gelpCode\').value = \'log.info("${Class.forName(\'org.apache.ivy.core.report.ResolveReport\').canonicalName}")\'')
//        configPage.executeJavaScript('document.getElementById(\'gelpCode\').value = \'log.info("${Class.forName(\'org.apache.ivy.core.report.ResolveReport\').canonicalName}")\'')
        configPage.executeJavaScript("document.getElementById('gelpCode').value = 'log.info(\"FOOBAR\")'")
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
