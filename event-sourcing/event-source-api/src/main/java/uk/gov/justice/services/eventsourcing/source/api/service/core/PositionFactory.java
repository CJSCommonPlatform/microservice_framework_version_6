package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

public class PositionFactory {

    public Position createPosition(final String position) {

        if (HEAD.equals(position)) {
            return head();
        }

        if (FIRST.equals(position)) {
            return first();
        }

        try {
            return position(valueOf(position));
        } catch (NumberFormatException e) {
            throw new BadRequestException(format("Position should be numeral, provided value: %s", position));
        }
    }
}
