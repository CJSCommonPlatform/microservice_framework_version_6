package uk.gov.justice.services.eventsourcing.repository.jdbc;


public enum FixedPosition {

    /*
     * Latest events
     */
    HEAD("HEAD"),

    /*
     * Oldest events
     */
    FIRST("1");


    private String position;

    FixedPosition(final String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }
}

