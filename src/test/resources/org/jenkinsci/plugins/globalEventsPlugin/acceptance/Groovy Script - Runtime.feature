Feature: Groovy Script - Runtime
  As a developer
  I want to be able to execute my groovy script
  So that I can have an action performed


  Scenario: I should be able to create a script, so that an action is performed.
    Given the script
    """
    log.info('Hello world!')
    """
    When the onStarted event is triggered
    Then the log level info should display 'Hello world!'


  # todo test Plugin Started / Stopped events
  @ignore
  Scenario Outline: Different event triggers should reflect in the 'event' parameter value, so that I can respond to specific events.
    Given the script
    """
    log.info("-> " + event)
    [actEvent: "-> " + event]
    """
    When the <Event> event is triggered
    Then the context should contain <Key> = <Value>
    Then the log level info should display '<Value>'

    Examples:
      | Event       | Key      | Value                      |
      | onStarted   | actEvent | -> RunListener.onStarted   |
      | onCompleted | actEvent | -> RunListener.onCompleted |
      | onFinalized | actEvent | -> RunListener.onFinalized |
      | onDeleted   | actEvent | -> RunListener.onDeleted   |


  Scenario: The plugin's package should be imported by default, so that I can make use of the 'Event' convenience class.
    Given the script
    """
    log.info("-> " + Event.JOB_STARTED)
    """
    When the onCompleted event is triggered
    Then the log level info should display '-> RunListener.onStarted'


  # todo Add mock Run, Listener, Jenkins, env vars etc
  Scenario: All relevant job parameters should passed into the script, so that I don't have to manually retrieve data.
    Given the script
    """
    log.info("${log.class}, $listener, $jenkins, $context, $run, $event, $env")
    """
    When the onStarted event is triggered
    Then the log level info should display 'class org.jenkinsci.plugins.globalEventsPlugin.LoggerTrap, null, null, [:], null, RunListener.onStarted, [:]'


  Scenario: If the script outputs a map, it should be placed into the cache for subsequent executions, so that the script can keep "memory".
    Given the script
    """
    [curr: event, prev: context.curr]
    """
    When the onStarted event is triggered
    Then the context should contain curr = RunListener.onStarted
    Then the context should contain prev = null
    When the onCompleted event is triggered
    Then the context should contain curr = RunListener.onCompleted
    Then the context should contain prev = RunListener.onStarted


  Scenario: The script should be able to import external dependencies, so that I don't have to reinvent the wheel.
    Given the script
    """
    @Grab('com.github.groovy-wslite:groovy-wslite:1.1.2')
    import wslite.soap.*
    log.info(SOAPClient.canonicalName)
    """
    When the onStarted event is triggered
    Then the log level info should display 'wslite.soap.SOAPClient'


  Scenario: Logging should be captured, so that there is an audit trail for me to debug retrospectively.
    Given the script
    """
    println 'Foobar1';
    System.out.println('Foobar2');
    System.err.println('Foobar3');
    log.info('Foobar4');
    """
    When the onStarted event is triggered
    Then the log level info should display 'Foobar4'


  # todo I wouldn't call throwing the exception graceful... should this be the expected behaviour?
  Scenario: Compilation exceptions should be handled gracefully, so that Jenkins doesn't have to.
    Given the script with exception
    """
    log.info('Hello
    """
    When the onStarted event is triggered
    Then an exception should be thrown with the message 'expecting ''', found '<EOF>' @ line 2, column 16'


  Scenario: Runtime exceptions should be handled gracefully, so that Jenkins doesn't have to.
    Given the script
    """
    throw new RuntimeException('foobar')
    """
    When the onStarted event is triggered
    Then no exception should be thrown
    Then the log level severe should display '>>> Caught unhandled exception! foobar'
