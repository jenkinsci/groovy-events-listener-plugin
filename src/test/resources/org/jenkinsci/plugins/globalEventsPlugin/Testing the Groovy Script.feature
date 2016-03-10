Feature: Testing the Groovy Script
  As a developer
  I want to be able to test my groovy script
  So that I get instant feedback on compilation errors/runtime errors/behaviour anomalies/etc

  # todo: test "testing" the script -> output validation message

  Scenario: I should be able to create a script, so that it is executed for specific events.
    Given the script
    """
    log.info('Hello world!')
    """
    When the script is executed
    Then the log should display 'Hello world!'

#  Scenario: Different event triggers should reflect in the 'event' parameter value, so that I can respond to only certain events.
#
#  Scenario: The plugin's package should be imported by default, so that I can make use of the 'Event' convenience class.
#
#  Scenario: All relevant job parameters should passed into the script, so that I don't have to manually retrieve data.
#
#  Scenario: Global environment variables should be present in the 'env' parameter, so that I don't have to manually retrieve data.
#
#  Scenario: If the script outputs a map, it should be placed into the cache for subsequent executions, so that the script can keep "memory".
#


  Scenario: The script should be able to import external dependencies, so that I don't have to reinvent the wheel.
    Given the script
    """
    @Grab('com.github.groovy-wslite:groovy-wslite:1.1.2')
    def x = 1 // <- hack, otherwise compiler complains...
    log.info(wslite.soap.SOAPClient.simpleName)
    """
    When the script is executed
    Then the log should display 'SOAPClient'


  Scenario: Logging should be captured, so that there is an audit trail for me to debug retrospectively.
    Given the script
    """
    println 'Foobar1';
    System.out.println('Foobar2');
    System.err.println('Foobar3');
    log.info('Foobar4');
    """
    When the script is executed
    Then the log should display 'Foobar4'


  Scenario: Compilation exceptions should be handled gracefully, so that Jenkins doesn't have to.
    Given the script
    """
    log.info('Hello
    """
    When the script is executed
    Then an exception should be thrown with the message
    """
    expecting ''', found '<EOF>' @ line 2, column 16
    """


  Scenario: Runtime exceptions should be handled gracefully, so that Jenkins doesn't have to.
    Given the script
    """
    throw new RuntimeException('foobar')
    """
    When the script is executed
    Then the script result should be ERROR with message
    """
    An exception was caught.<br><br>java.lang.RuntimeException: foobar
    """
