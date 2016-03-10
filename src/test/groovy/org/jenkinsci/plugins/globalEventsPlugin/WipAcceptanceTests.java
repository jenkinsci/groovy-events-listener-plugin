package org.jenkinsci.plugins.globalEventsPlugin;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        strict = false,
        plugin = {"pretty"},
        tags = {"@wip", "~@ignore"}
)
public class WipAcceptanceTests {
}
