package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.sender.Sender;

public interface SenderFactory {
    Sender createSender(final Component componentDestination);
}
