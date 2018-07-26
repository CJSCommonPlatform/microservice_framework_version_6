package uk.gov.justice.services.eventsourcing.publishing;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventDeQueuerTimerBeanTest {

    private static final String TIMER_START_VALUE = "7000";
    private static final String TIMER_INTERVAL_VALUE = "2000";

    @Mock
    private TimerService timerService;

    @Mock
    private TimerConfigFactory timerConfigFactory;

    @Mock
    private EventDeQueuerAndPublisher eventDeQueuerAndPublisher;


    @InjectMocks
    private EventDeQueuerTimerBean eventDeQueuerTimerBean;

    @Test
    public void shouldRunPublishUntilNoAllEventsArePublished() throws Exception {

        when(eventDeQueuerAndPublisher.deQueueAndPublish()).thenReturn(true, true, false);

        eventDeQueuerTimerBean.doDeQueueAndPublish();

        verify(eventDeQueuerAndPublisher, times(3)).deQueueAndPublish();
    }

    @Test
    public void shouldCancelAnyRunningJobsAndStartTheNewJobOnInitialization() throws Exception {

        final String timerJobName = "framework.de-queue-events-and-publish.job";

        final Timer timer = mock(Timer.class);
        final TimerConfig timerConfig = mock(TimerConfig.class);

        eventDeQueuerTimerBean.timerStartWaitMilliseconds = TIMER_START_VALUE;
        eventDeQueuerTimerBean.timerIntervalMilliseconds = TIMER_INTERVAL_VALUE;

        when(timerService.getAllTimers()).thenReturn(singletonList(timer));
        when(timer.getInfo()).thenReturn(timerJobName);
        when(timerConfigFactory.createNew()).thenReturn(timerConfig);


        eventDeQueuerTimerBean.startTimerService();

        final InOrder inOrder = inOrder(timer, timerConfig, timerService);

        inOrder.verify(timer).cancel();
        inOrder.verify(timerConfig).setPersistent(false);
        inOrder.verify(timerConfig).setInfo(timerJobName);
        inOrder.verify(timerService).createIntervalTimer(7_000, 2_000, timerConfig);

    }
}
