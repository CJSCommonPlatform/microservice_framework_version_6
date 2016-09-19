package uk.gov.justice.services.event.buffer.core.service;

import java.util.UUID;

public interface BufferInitialisationStrategy {


    /**
     * Initialises buffer (if not already intialised) and returns the current version of the buffer
     * status
     *
     * @param streamId - id of the stream to be initialised
     * @return - version of the last event that was in order
     */
    long initialiseBuffer(final UUID streamId);


}
