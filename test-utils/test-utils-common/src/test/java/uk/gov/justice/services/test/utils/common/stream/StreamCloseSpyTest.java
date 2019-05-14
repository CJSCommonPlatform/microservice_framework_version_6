package uk.gov.justice.services.test.utils.common.stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class StreamCloseSpyTest {


    private StreamCloseSpy streamCloseSpy = new StreamCloseSpy();

    @Test
    public void shouldSetStreamClosedToTrueOnRun() throws Exception {

        assertThat(streamCloseSpy.streamClosed(), is(false));

        streamCloseSpy.run();

        assertThat(streamCloseSpy.streamClosed(), is(true));

    }
}
