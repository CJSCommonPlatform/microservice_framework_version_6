package uk.gov.justice.services.messaging;

/**
 * Interface for a messaging envelope containing metadata and a payload.
 *
 * @param <T> the type of payload this envelope can contain
 */
public interface Envelope<T> {

    Metadata metadata();

    T payload();

}
