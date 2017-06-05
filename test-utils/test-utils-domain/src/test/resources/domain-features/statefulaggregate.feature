Feature: StatefulAggregate
  # Issue with "the"  in Then
  # Issue with "no events" in Then

  Scenario: Previous events are applied if given, but if no previous event is given then no new event occurs

    Given no previous events
    When doSomething to a uk.gov.justice.services.domain.aggregate.StatefulAggregate using argsA
    #Then the no events

  Scenario: Previous events are applied, so new event occurs

    Given there are previous events something-happened
    When doSomething to a uk.gov.justice.services.domain.aggregate.StatefulAggregate using argsA
    Then the something-else-happened

  Scenario: Previous events are applied, so new event occurs with primitive arguments passed

    Given there are previous events something-happened
    When doSomething to a uk.gov.justice.services.domain.aggregate.StatefulAggregate using argsA
    Then the something-else-happened
