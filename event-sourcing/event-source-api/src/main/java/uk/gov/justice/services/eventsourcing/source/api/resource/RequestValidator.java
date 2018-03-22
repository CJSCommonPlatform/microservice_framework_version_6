package uk.gov.justice.services.eventsourcing.source.api.resource;

import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;


public final class RequestValidator {

    private static final String BAD_HEAD_REQUEST = "Invalid request, cannot request PREVIOUS page from HEAD.";
    private static final String BAD_FIRST_REQUEST = "Invalid request, cannot request NEXT page from FIRST.";

    private RequestValidator() {
    }

    public static void validateRequest(final String position, final String direction) {
        if (HEAD.equals(position) && FORWARD.toString().equals(direction)) {
            throw new BadRequestException(BAD_HEAD_REQUEST);
        }

        if (FIRST.equals(position) && BACKWARD.toString().equals(direction)) {
            throw new BadRequestException(BAD_FIRST_REQUEST);
        }
    }
}
