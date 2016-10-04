package uk.gov.justice.services.test.utils.core.messaging;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.Poller.DEFAULT_DELAY_INTERVAL_MILLIS;
import static uk.gov.justice.services.test.utils.core.messaging.Poller.DEFAULT_RETRY_COUNT;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unchecked")
public class PollerTest {

    private Sleeper sleeper = mock(Sleeper.class);

    private Poller poller = new Poller(DEFAULT_RETRY_COUNT, DEFAULT_DELAY_INTERVAL_MILLIS, sleeper);

    @Test
    public void shouldPollUntilFound() throws Exception {

        final String result = "you found me!";
        final Supplier<Optional<String>> supplier = mock(Supplier.class);

        when(supplier.get()).thenReturn(empty(), empty(), empty(), empty(), of(result));

        final Optional<String> found = poller.pollUntilFound(supplier);

        assertThat(found.isPresent(), is(true));
        assertThat(found.get(), is(result));

        verify(sleeper, times(4)).sleepFor(DEFAULT_DELAY_INTERVAL_MILLIS);
    }

    @Test
    public void shouldReturnEmptyIfNotFoundAfterFiveAttempts() throws Exception {

        final Supplier<Optional<String>> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(empty(), empty(), empty(), empty(), empty());

        final Optional<String> found = poller.pollUntilFound(supplier);

        assertThat(found.isPresent(), is(false));

        verify(sleeper, times(5)).sleepFor(DEFAULT_DELAY_INTERVAL_MILLIS);
    }

    @Test
    public void shouldPollUntilNotFound() throws Exception {

        final String result = "you found me!";
        final Supplier<Optional<String>> supplier = mock(Supplier.class);

        when(supplier.get()).thenReturn(of(result), of(result), of(result), of(result), empty());

        poller.pollUntilNotFound(supplier);

        verify(sleeper, times(4)).sleepFor(DEFAULT_DELAY_INTERVAL_MILLIS);
    }

    @Test
    public void shouldThrowExceptionIfStillFoundAfterFiveAttempts() throws Exception {

        final String result = "you found me!";
        final Supplier<Optional<String>> supplier = mock(Supplier.class);

        when(supplier.get()).thenReturn(of(result), of(result), of(result), of(result), of(result));

        try {
            poller.pollUntilNotFound(supplier);
            fail();
        } catch (AssertionError expected) {
        }

        verify(sleeper, times(5)).sleepFor(DEFAULT_DELAY_INTERVAL_MILLIS);
    }
}
