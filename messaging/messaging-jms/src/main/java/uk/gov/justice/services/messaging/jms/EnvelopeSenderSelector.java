package uk.gov.justice.services.messaging.jms;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EnvelopeSenderSelector {

    @Inject
    private JmsSender jmsSender;

    @Inject
    private ShutteringStoreSender shutteringStoreSender;

    private AtomicBoolean shuttered = new AtomicBoolean(false);

    public EnvelopeSender getEnvelopeSender() {

        if (shuttered.get()) {
            return shutteringStoreSender;
        }

        return jmsSender;
    }

    public void setShuttered(final boolean shuttered) {
        this.shuttered.set(shuttered);
    }
}
