package uk.gov.justice.services.fileservice.repository;

public class Closer {

    public void close(final AutoCloseable autoCloseable, final AutoCloseable... autoCloseables) {
        close(autoCloseable);
        for(final AutoCloseable closeable: autoCloseables) {
            close(closeable);
        }
    }

    private void close(final AutoCloseable autoCloseable) {
        if (autoCloseable == null) {
            return;
        }

        try {
           autoCloseable.close();
        } catch (final Exception  ignored) {}
    }
}
