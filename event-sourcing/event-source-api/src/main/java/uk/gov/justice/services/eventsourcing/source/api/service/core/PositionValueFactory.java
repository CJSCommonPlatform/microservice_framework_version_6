package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.lang.String.valueOf;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;

public class PositionValueFactory {

    public String getPositionValue(final Position position) {
        if (position.isHead()) {
            return HEAD;
        }
        if (position.isFirst()) {
            return FIRST;
        }
        return valueOf(position.getSequenceId());
    }
}
