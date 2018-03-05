package uk.gov.justice.services.event.buffer.core.service;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

import java.util.UUID;

public class PostgreSQLBasedBufferInitialisationStrategy implements BufferInitialisationStrategy {
    private static final long INITIAL_VERSION = 0L;

    private final StreamStatusJdbcRepository streamStatusRepository;

    public PostgreSQLBasedBufferInitialisationStrategy(final StreamStatusJdbcRepository streamStatusRepository) {
        this.streamStatusRepository = streamStatusRepository;
    }

    @Override
    public long initialiseBuffer(final UUID streamId, final String source) {
        streamStatusRepository.insertOrDoNothing(new StreamStatus(streamId, INITIAL_VERSION, source));
        return streamStatusRepository.findByStreamIdAndSource(streamId, source)
                .orElseThrow(() -> new IllegalStateException("stream status cannot be empty"))
                .getVersion();
    }
}
