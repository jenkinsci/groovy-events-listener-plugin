package org.jenkinsci.plugins.globalEventsPlugin.acceptance;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        tags = "@wip and not @ignore"
)
public class WipAcceptanceTests {
}
