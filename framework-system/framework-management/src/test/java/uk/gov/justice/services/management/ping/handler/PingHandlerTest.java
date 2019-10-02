package uk.gov.justice.services.management.ping.handler;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.PingCommand;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

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

        pingHandler.ping(new PingCommand(), randomUUID());

        verify(logger).info("********** Received system command 'PING' at 2019-11-07T15:22:25Z **********");
    }
}
