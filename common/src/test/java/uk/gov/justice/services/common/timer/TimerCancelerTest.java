package uk.gov.justice.services.common.timer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TimerCancelerTest {

    @InjectMocks
    private TimerCanceler timerCanceler;

    @Test
    public void shouldCancelTheTimerWithTheSpecifiedName() throws Exception {

        final String timerJobName_1 = "timer 1";
        final String timerJobName_2 = "timer 2";
        final String timerJobName_3 = "timer 3";

        final TimerService timerService = mock(TimerService.class);
        final Timer timer_1 = mock(Timer.class);
        final Timer timer_2 = mock(Timer.class);
        final Timer timer_3 = mock(Timer.class);

        when(timerService.getAllTimers()).thenReturn(asList(timer_1, timer_2, timer_3));

        when(timer_1.getInfo()).thenReturn(timerJobName_1);
        when(timer_2.getInfo()).thenReturn(timerJobName_2);
        when(timer_3.getInfo()).thenReturn(timerJobName_3);

        timerCanceler.cancelTimer(timerJobName_2, timerService);

        verify(timer_2).cancel();

        verify(timer_1, never()).cancel();
        verify(timer_3, never()).cancel();
    }
}
