package uk.gov.justice.services.eventsourcing.publishing;

import javax.ejb.TimerConfig;

public class TimerConfigFactory {

    public TimerConfig createNew() {
        return new TimerConfig();
    }
}
