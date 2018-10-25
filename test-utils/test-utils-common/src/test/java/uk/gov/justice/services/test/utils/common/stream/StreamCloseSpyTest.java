package uk.gov.justice.services.test.utils.common.stream;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


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
