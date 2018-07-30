package uk.gov.justice.services.eventsourcing.publishing;

import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.GlobalValue;

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

    private static final int THRESHOLD = 10;
    private static final String TIMER_JOB = "framework.de-queue-events-and-publish.job";

    @Inject
    @GlobalValue(key = "timer.start.wait.milliseconds", defaultValue = "7000")
    String timerStartWaitMilliseconds;

    @Inject
    @GlobalValue(key = "timer.interval.milliseconds", defaultValue = "2000")
    String timerIntervalMilliseconds;

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
            isOverlappingLimitReached();
        }
    }

    private void createIntervalTimer() {
        final TimerConfig timerConfig = timerConfigFactory.createNew();
        timerConfig.setPersistent(false);
        timerConfig.setInfo(TIMER_JOB);

        timerService.createIntervalTimer(parseLong(timerStartWaitMilliseconds), parseLong(timerIntervalMilliseconds), timerConfig);
    }

    private void cancelExistingTimer() {
        timerService.getAllTimers().stream().filter(t -> TIMER_JOB.equals(t.getInfo())).forEach(Timer::cancel);
    }

    private void isOverlappingLimitReached(){
        if(!timerService.getAllTimers().isEmpty() && timerService.getAllTimers().size() > THRESHOLD){
            cancelExistingTimer();
        }
    }
}
