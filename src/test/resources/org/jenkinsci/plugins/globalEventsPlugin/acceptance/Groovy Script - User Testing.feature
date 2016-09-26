Feature: Groovy Script - User Testing
  As a developer
  I want to be able to test my groovy script
  So that I get instant feedback on compilation errors/runtime errors/behaviour anomalies/etc


  Scenario: I should be able to create a script, so that an action is performed
    Given the script
    """
    log.info('Hello world!')
    """
    When I test the script
    Then the validation result should be OK with message 'Hello world!'


  Scenario: The plugin's package should be imported by default, so that I can make use of the 'Event' convenience class
    Given the script
    """
    log.info("- " + Event.JOB_STARTED)
    """
    When I test the script
    Then the validation result should be OK with message '- RunListener.onStarted'


  Scenario: The script should be able to import external dependencies, so that I don't have to reinvent the wheel
    Given the script
    """
    @Grab('com.github.groovy-wslite:groovy-wslite:1.1.2')
    import wslite.soap.*
    log.info(SOAPClient.canonicalName)
    """
    When I test the script
    Then the validation result should be OK with message 'wslite.soap.SOAPClient'


  Scenario: Compilation exceptions should be handled gracefully, so that Jenkins doesn't have to
    Given the script with exception
    """
    log.info('Hello
    """
    When I test the script
    Then the validation result should be ERROR with message 'An exception was caught.'
    Then the validation result should be ERROR with message 'expecting &#039;&#039;&#039;, found &#039;&lt;EOF&gt;&#039; @ line 2, column 16.'


  Scenario: Runtime exceptions should be handled gracefully, so that Jenkins doesn't have to
    Given the script
    """
    throw new RuntimeException('foobar')
    """
    When I test the script
    Then the validation result should be ERROR with message 'foobar'

