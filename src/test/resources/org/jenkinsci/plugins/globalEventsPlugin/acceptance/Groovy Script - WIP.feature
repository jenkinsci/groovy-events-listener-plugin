Feature: Groovy Script - WIP
  As a developer
  I want isolated WIP scenarios
  So that WIP runs only what is actively being worked on

  @wip
  Scenario: The script should have access to the Ivy ResolveReport class, So that @Grab works
    Given the script
    """
    log.info("-> ${Class.forName('org.apache.ivy.core.report.ResolveReport').canonicalName}")
    """
    When the Run.onStarted event is triggered
    Then the log level info should display '-> org.apache.ivy.core.report.ResolveReport'

