package uk.gov.justice.services.jdbc.persistence;

import java.util.Map;
import java.util.stream.Stream;

public interface PaginationCapableRepository<E> {
    Stream<E> getPage(final long offset, final long pageSize, final Map<String, Object> params);
}