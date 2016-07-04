package org.jenkinsci.plugins.globalEventsPlugin.integration

import org.hamcrest.core.Is
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import static org.hamcrest.MatcherAssert.assertThat

public class IntegrationTest {

    @Test
    public void seleniumTest() {
        def driver = new HtmlUnitDriver()
//        def driver = new FirefoxDriver()
        try {
            // open page...
            def wait = new WebDriverWait(driver, 60)
            driver.get("http://localhost:8080/")
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Welcome to Jenkins!"))
            driver.get("http://localhost:8080/configure")
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Groovy Events Listener Plugin"))

            // set the groovy code...
            driver.executeScript("document.getElementById('gelpCode').value = arguments[0]", "log.info('FOOBAR')")
//            driver.executeScript("document.getElementById('gelpCode').codemirrorObject.setValue(arguments[0])", "log.info('FOOBAR')")

            // do "test code"...
            assertThat(driver.findElementsByXPath("//div[@class='ok']").size(), Is.is(0))
            assertThat(driver.findElementsByXPath("//div[@class='error']").size(), Is.is(0))
            driver.findElementByXPath("//button[.='Test Groovy Code']").click()

            // verify output...
            assertThat(driver.findElementsByXPath("//div[@class='ok']").size(), Is.is(1))
            assertThat(driver.findElementByXPath("//div[@class='ok']").getText().replaceAll("\\d+", "X"), Is.is('''Execution completed successfully!

>>> Executing groovy script - parameters: [env, run, jenkins, log, event, context]

FOOBAR

>>> Ignoring response - value is null or not a Map. response=null

>>> Executing groovy script completed successfully. totalDurationMillis='X',executionDurationMillis='X',synchronizationMillis=`X`'''))
        } finally {
            driver.windowHandles.each {
                driver.switchTo().window(it).close()
            }
            driver.quit()
        }
    }
}
