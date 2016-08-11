package uk.gov.justice.services.core.it.util.sender;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.SenderFactory;
import uk.gov.justice.services.core.sender.Sender;


public class TestSenderFactory implements SenderFactory {



    @Override
    public Sender createSender(final Component componentDestination) {
        return RecordingSender.instance();
    }
}
