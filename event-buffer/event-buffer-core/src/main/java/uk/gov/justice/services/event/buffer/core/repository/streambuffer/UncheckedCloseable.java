package uk.gov.justice.services.event.buffer.core.repository.streambuffer;


import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

public interface UncheckedCloseable extends Runnable, AutoCloseable {
    default void run() {
        try {
            close();
        } catch (Exception ex) {
            throw new JdbcRepositoryException(ex);
        }
    }

    static UncheckedCloseable wrap(AutoCloseable c) {
        return c::close;
    }

    default UncheckedCloseable nest(AutoCloseable c) {
        return () -> {
            try (UncheckedCloseable c1 = this) {
                c.close();
            }
        };
    }
}
