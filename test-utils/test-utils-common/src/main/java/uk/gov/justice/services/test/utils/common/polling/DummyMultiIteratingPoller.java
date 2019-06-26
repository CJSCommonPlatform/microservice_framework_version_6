package uk.gov.justice.services.test.utils.common.polling;

import uk.gov.justice.services.common.polling.MultiIteratingPoller;

import java.util.function.BooleanSupplier;

public class DummyMultiIteratingPoller extends MultiIteratingPoller {

    public DummyMultiIteratingPoller() {
        super(1, 100L, null, null);
    }

    public boolean pollUntilTrue(final BooleanSupplier conditionalFunction) {
        return conditionalFunction.getAsBoolean();
    }
}
