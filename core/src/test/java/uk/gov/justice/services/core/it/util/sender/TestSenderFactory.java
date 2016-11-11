package uk.gov.justice.services.core.it.util.sender;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.sender.SenderFactory;


public class TestSenderFactory implements SenderFactory {

    @Override
    public Sender createSender(final String componentDestination) {
        return RecordingSender.instance();
    }
}
