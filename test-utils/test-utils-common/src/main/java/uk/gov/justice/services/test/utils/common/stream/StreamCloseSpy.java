package uk.gov.justice.services.test.utils.common.stream;

/**
 * Class to be used in unit tests to check if a stream has been closed.
 *
 * Usage:
 * <pre>
 * {@code
 *   stream.onClose(streamCloseSpy);
 *   assertThat(streamCloseSpy.streamClosed(), is(true));
 * }
 * </pre>
 */
public class StreamCloseSpy implements Runnable {
    private boolean streamClosed = false;

    @Override
    public void run() {
        streamClosed = true;
    }

    public boolean streamClosed() {
        return streamClosed;
    }

}