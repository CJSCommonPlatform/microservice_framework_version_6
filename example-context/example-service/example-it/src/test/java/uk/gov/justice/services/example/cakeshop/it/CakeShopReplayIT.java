package uk.gov.justice.services.example.cakeshop.it;

import org.junit.Test;

public class CakeShopReplayIT {

    @Test
    public void shouldPauseAndAllowReplayToComplete() throws Exception {
        Thread.sleep(10_000);
    }
}
