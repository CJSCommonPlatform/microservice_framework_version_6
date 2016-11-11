package uk.gov.justice.services.core.sender;

public interface SenderFactory {

    Sender createSender(final String componentDestination);
}
