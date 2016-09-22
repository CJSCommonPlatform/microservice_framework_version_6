package uk.gov.justice.services.event.buffer.core.service;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

import java.util.Optional;
import java.util.UUID;

public class AnsiSQLBasedBufferInitialisationStrategy implements BufferInitialisationStrategy {

    private static final long INITIAL_VERSION = 0L;
    private final StreamStatusJdbcRepository streamStatusRepository;

    public AnsiSQLBasedBufferInitialisationStrategy(final StreamStatusJdbcRepository streamStatusRepository) {
        this.streamStatusRepository = streamStatusRepository;
    }

    @Override
    public long initialiseBuffer(final UUID streamId) {

        final Optional<StreamStatus> currentStatus = streamStatusRepository.findByStreamId(streamId);

        if (!currentStatus.isPresent()) {
            //this is to address race condition
            //in case of primary key violation the execption gets thrown, event goes back into topic and the transaction gets retried
            streamStatusRepository.insert(new StreamStatus(streamId, INITIAL_VERSION));
            return INITIAL_VERSION;

        } else {
            return currentStatus.get().getVersion();
        }
    }



}
