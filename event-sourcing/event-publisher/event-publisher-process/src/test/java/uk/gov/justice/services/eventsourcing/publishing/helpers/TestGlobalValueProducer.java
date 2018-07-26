package uk.gov.justice.services.eventsourcing.publishing.helpers;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;

import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.NamingException;

public class TestGlobalValueProducer extends  GlobalValueProducer {

    private static final String TIMER_START_KEY = "timer.start.wait.milliseconds";
    private static final String TIMER_START_VALUE = "7000";
    private static final String TIMER_INTERVAL_KEY = "timer.interval.milliseconds";
    private static final String TIMER_INTERVAL_VALUE = "2000";

    public TestGlobalValueProducer() throws NamingException {
    }

    @GlobalValue
    @Produces
    public String stringValueOf(final InjectionPoint ip) throws NamingException {
        final Optional<String> annotationKey = Optional.of(ip.getAnnotated().getAnnotation(GlobalValue.class).key());
        if(annotationKey.isPresent()){
            if (annotationKey.get().equals(TIMER_START_KEY)) {
                return TIMER_START_VALUE;
            }

            if (annotationKey.get().equals(TIMER_INTERVAL_KEY)) {
                return TIMER_INTERVAL_VALUE;
            }
        }
        return super.stringValueOf(ip);
    }
}
