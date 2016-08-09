package uk.gov.justice.services.event.buffer.core.repository.spliterator;

import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 *
 * Spliterator enables a mutatable stream which returns a consecutive stream of events.
 * If the stream loads an event which is not consecutive it will close without loading
 * any other events.
 *
 */
public class EventBufferSpliterator {
    private long currentVersion;
    private final Iterator<StreamBufferEvent> bufferEventIterator;

    public EventBufferSpliterator(long currentVersion, Iterator<StreamBufferEvent> bufferEventIterator) {
        this.currentVersion = currentVersion;
        this.bufferEventIterator = bufferEventIterator;
    }

    public Spliterators.AbstractSpliterator<StreamBufferEvent> invoke() {
        return new Spliterators.AbstractSpliterator<StreamBufferEvent>(
                Long.MAX_VALUE, Spliterator.ORDERED) {

            @Override
            public boolean tryAdvance(final Consumer<? super StreamBufferEvent> consumer) {
                StreamBufferEvent next = null;
                if (!bufferEventIterator.hasNext()) {
                    return false;
                } else {
                    next = bufferEventIterator.next();
                    final long version = next.getVersion();
                    if (version - currentVersion > 1) {
                        return false;
                    }
                    currentVersion = version;

                    consumer.accept(next);

                    return true;
                }
            }
        };
    }
}