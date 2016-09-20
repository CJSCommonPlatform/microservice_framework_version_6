package uk.gov.justice.services.test.utils.core.helper;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class Sleeper {

    public void sleepFor(final long milliseconds) {
        try {
            sleep(milliseconds);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
