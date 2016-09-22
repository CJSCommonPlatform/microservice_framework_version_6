package uk.gov.justice.services.event.buffer.it.util.buffer;


import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.event.buffer.core.service.AnsiSQLBasedBufferInitialisationStrategy;
import uk.gov.justice.services.event.buffer.core.service.BufferInitialisationStrategy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class AnsiSQLBufferInitialisationStrategyProducer {

    @Inject
    StreamStatusJdbcRepository streamStatusRepository;

    @Produces
    public BufferInitialisationStrategy bufferInitialisationStrategy() {
        return new AnsiSQLBasedBufferInitialisationStrategy(streamStatusRepository);
    }
}
