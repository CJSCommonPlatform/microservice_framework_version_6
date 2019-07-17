package uk.gov.justice.services.management.ping.handler;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
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
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.PingSystemCommand;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.inject.Inject;


@RunWith(MockitoJUnitRunner.class)
public class PingHandlerTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private PingHandler pingHandler;

    @Test
    public void shouldLogAPingMessage() throws Exception {

        final ZonedDateTime now = of(2019, 11, 7, 15, 22, 25, 0, UTC);

        when(clock.now()).thenReturn(now);

        pingHandler.ping(new PingSystemCommand());

        verify(logger).info("********** Received system command 'PING' at 2019-11-07T15:22:25Z **********");
    }
}
