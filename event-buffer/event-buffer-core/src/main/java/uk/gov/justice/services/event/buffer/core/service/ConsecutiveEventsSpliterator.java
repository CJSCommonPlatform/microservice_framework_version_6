package uk.gov.justice.services.event.buffer.core.service;

import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;

import java.util.Iterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Spliterator enables to transform stream of events into a consecutive stream of events.
 * If a version gap in the eventStream is spotted then then the processing of the stream terminates.
 */
public class ConsecutiveEventsSpliterator extends AbstractSpliterator<StreamBufferEvent> {
    private long currentVersion;
    private final Stream<StreamBufferEvent> eventStream;
    private final Iterator<StreamBufferEvent> eventStreamIterator;

    public ConsecutiveEventsSpliterator(final Stream<StreamBufferEvent> eventStream, final long currentVersion) {
        super(Long.MAX_VALUE, ORDERED);
        this.eventStream = eventStream;
        this.currentVersion = currentVersion;
        this.eventStreamIterator = eventStream.iterator();
    }

    @Override
    public boolean tryAdvance(final Consumer<? super StreamBufferEvent> consumer) {
        if (!eventStreamIterator.hasNext()) {
            eventStream.close();
            return false;
        } else {
            final StreamBufferEvent next = eventStreamIterator.next();
            final long version = next.getVersion();
            if (versionGapFound(version)) {
                eventStream.close();
                return false;
            } else {
                currentVersion = version;
                consumer.accept(next);
                return true;
            }
        }
    }

    private boolean versionGapFound(final long version) {
        return version - currentVersion > 1;
    }
}