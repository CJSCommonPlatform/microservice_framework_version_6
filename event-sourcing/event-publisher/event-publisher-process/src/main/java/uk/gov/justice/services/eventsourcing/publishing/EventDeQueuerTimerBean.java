package uk.gov.justice.services.eventsourcing.publishing;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

@Singleton
@Startup
public class EventDeQueuerTimerBean {

    private static final String TIMER_JOB = "framework.de-queue-events-and-publish.job";

    private static final long TIMER_START_WAIT_MILLISECONDS = 7 * 1_000L;
    private static final long TIMER_INTERVAL_MILLISECONDS = 1 * 1_000L;

    @Resource
    TimerService timerService;

    @Inject
    TimerConfigFactory timerConfigFactory;

    @Inject
    EventDeQueuerAndPublisher eventDeQueuerAndPublisher;

    @PostConstruct
    public void startTimerService() {
        cancelExistingTimer();
        createIntervalTimer();
    }

    @Timeout
    @SuppressWarnings({"StatementWithEmptyBody", "unused"})
    public void doDeQueueAndPublish() {

        while (eventDeQueuerAndPublisher.deQueueAndPublish()) {
            // nothing to do. The work happens in deQueueAndPublish()
            // but it needs to be transactional.
        }
    }

    private void createIntervalTimer() {
        final TimerConfig timerConfig = timerConfigFactory.createNew();
        timerConfig.setPersistent(false);
        timerConfig.setInfo(TIMER_JOB);

        timerService.createIntervalTimer(TIMER_START_WAIT_MILLISECONDS, TIMER_INTERVAL_MILLISECONDS, timerConfig);
    }

    private void cancelExistingTimer() {
        timerService.getAllTimers().stream().filter(t -> TIMER_JOB.equals(t.getInfo())).forEach(Timer::cancel);
    }
}
