package uk.gov.justice.services.core.it.util.sender;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.SenderFactory;
import uk.gov.justice.services.core.sender.Sender;

import javax.inject.Inject;


public class TestSenderFactory implements SenderFactory {

    @Inject
    RecordingSender recordingSender;

    @Override
    public Sender createSender(final Component componentDestination) {
        return recordingSender;
    }
}
