package uk.gov.justice.services.eventsourcing.source.core;

import java.util.Arrays;
import java.util.stream.Stream;

public final class Events {

    private Events() {
    }

    public static Stream<Object> streamOf(final Object... objects) {
        return Arrays.stream(objects);
    }

}
