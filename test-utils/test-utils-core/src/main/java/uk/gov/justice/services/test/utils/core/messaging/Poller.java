package uk.gov.justice.services.test.utils.core.messaging;

import static java.util.Optional.empty;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;

/**
 * Test utility class for polling a function interface until an expected resource is
 * found/not found. Will poll five times with a one second pause between polls.
 *
 * Takes a functional interface which must return an java Optional
 *
 * To use:
 *
 * <pre>
 *  {@code
 *
 *      private final MyResourceFinder myResourceFinder = new MyResourceFinder();
 *      private final Poller poller = new Poller();
 *
 *      {@literal @}Test
 *      public void myTest() {
 *
 *          final Optional<String> found = poller.pollUntilFound(myResourceFinder::find);
 *      }
 *
 *  }
 * </pre>
 */
public class Poller {

    static final int DEFAULT_RETRY_COUNT = 5;
    static final long DEFAULT_DELAY_INTERVAL_MILLIS = 1000L;

    private final Sleeper sleeper;

    private final int retryCount;
    private final long delayIntervalMillis;

    /**
     * Creates a Poller with a default retry count of 5 and default delay
     * interval of 1 second
     */
    public Poller() {
        this(DEFAULT_RETRY_COUNT, DEFAULT_DELAY_INTERVAL_MILLIS);
    }

    /**
     * Creates a Poller with the specifed retry count and delay interval
     *
     * @param retryCount          the number of times the resource will be polled
     * @param delayIntervalMillis the delay in milliseconds between each poll
     */
    public Poller(final int retryCount, final long delayIntervalMillis) {
        this(retryCount, delayIntervalMillis, new Sleeper());
    }

    @VisibleForTesting
    Poller(final int retryCount, final long delayIntervalMillis, final Sleeper sleeper) {
        this.sleeper = sleeper;
        this.retryCount = retryCount;
        this.delayIntervalMillis = delayIntervalMillis;
    }

    /**
     * Polls a Supplier until the supplied resource is found
     *
     * @param finderFunction A Supplier functional interface for supplying the polled resource. Must
     *                       return a java Optional.
     * @param <T>            The Type of the supplied resource
     * @return an Optional containing the found resource, or empty if not found.
     */
    public <T> Optional<T> pollUntilFound(final Supplier<Optional<T>> finderFunction) {

        for (int i = 0; i < retryCount; i++) {
            final Optional<T> found = finderFunction.get();

            if (found.isPresent()) {
                return found;
            }

            sleeper.sleepFor(delayIntervalMillis);
        }

        return empty();
    }

    /**
     * Polls a Supplier until the supplied resource is no longer found
     *
     * @param finderFunction A Supplier functional interface for supplying the polled resource. Must
     *                       return a java Optional.
     * @param <T>            The Type of the supplied resource.
     * @throws AssertionError if the resource is still found after five attempts.
     */
    public <T> void pollUntilNotFound(final Supplier<Optional<T>> finderFunction) {

        Optional<T> found = empty();
        for (int i = 0; i < retryCount; i++) {
            found = finderFunction.get();
            if (!found.isPresent()) {
                return;
            }

            sleeper.sleepFor(delayIntervalMillis);
        }

        throw new AssertionError(found.get() + " still found in database after " + DEFAULT_RETRY_COUNT + " attempts");
    }
}
