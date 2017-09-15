package uk.gov.justice.services.jdbc.persistence;

import java.util.Map;
import java.util.stream.Stream;

public interface PaginationCapableRepository<E> {
    Stream<E> getFeed(final long offset,
                      final Link link,
                      final long pageSize,
                      final Map<String, Object> params);

    boolean recordExists(final long offset,
                        final Link link,
                        final long pageSize,
                        final Map<String, Object> params);
}