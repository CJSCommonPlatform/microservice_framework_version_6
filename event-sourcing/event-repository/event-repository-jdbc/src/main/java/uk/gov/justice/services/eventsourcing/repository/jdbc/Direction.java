package uk.gov.justice.services.eventsourcing.repository.jdbc;


public enum Direction {

    /*
     * towards latest events
     */
    FORWARD,

    /*
     * towards oldest events
     */
    BACKWARD
}

