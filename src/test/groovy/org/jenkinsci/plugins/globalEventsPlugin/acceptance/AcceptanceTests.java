package org.jenkinsci.plugins.globalEventsPlugin.acceptance;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        tags = "not @ignore"
)
public class AcceptanceTests {
    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();
}
