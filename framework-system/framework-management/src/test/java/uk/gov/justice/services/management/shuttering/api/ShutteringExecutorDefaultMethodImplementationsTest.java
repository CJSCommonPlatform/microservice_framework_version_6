package uk.gov.justice.services.management.shuttering.api;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.process.ShutteringExecutorWithNoImplementations;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringExecutorDefaultMethodImplementationsTest {

    @Test
    public void shouldReturnClassSimpleNameForTheNameByDefault() throws Exception {
        assertThat(new ShutteringExecutorWithNoImplementations().getName(), is(ShutteringExecutorWithNoImplementations.class.getSimpleName()));
    }

    @Test
    public void shouldShutterShouldReturnFalseByDefault() throws Exception {
        assertThat(new ShutteringExecutorWithNoImplementations().shouldShutter(), is(false));
    }

    @Test
    public void shouldUnshutterShouldReturnFalseByDefault() throws Exception {
        assertThat(new ShutteringExecutorWithNoImplementations().shouldUnshutter(), is(false));
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionBuDefaultForShutter() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        try {
            new ShutteringExecutorWithNoImplementations().shutter(commandId, systemCommand);
            fail();
        } catch (final UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), is("Method not implemented"));
        }
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionBuDefaultForUnshutter() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        try {
            new ShutteringExecutorWithNoImplementations().unshutter(commandId, systemCommand);
            fail();
        } catch (final UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), is("Method not implemented"));
        }
    }
}
