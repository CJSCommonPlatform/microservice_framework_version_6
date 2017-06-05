Feature: StatelessAggregate
  # Issue with "the"  in Then
  # Issue with "no events" in Then

  Scenario: No previous events

    Given no previous events
    When doSomething to a uk.gov.justice.services.domain.aggregate.StatelessAggregate using argsA
    Then the something-happened

  Scenario: One previous event

    Given there are previous events something-happened
    When doSomething to a uk.gov.justice.services.domain.aggregate.StatelessAggregate using argsA
    Then the something-happened

  Scenario: Two previous events

    Given there are previous events something-happened,something-happened
    When doSomething to a uk.gov.justice.services.domain.aggregate.StatelessAggregate using argsA
    Then the something-happened

  Scenario: No new events

    Given there are previous events something-happened
    When doNotDoSomething to a uk.gov.justice.services.domain.aggregate.StatelessAggregate using argsA
    #Then the no events occurred

  Scenario: Two new events

    Given there are previous events something-happened
    When doSomethingTwice to a uk.gov.justice.services.domain.aggregate.StatelessAggregate using argsA
    Then the something-happened,something-happened

