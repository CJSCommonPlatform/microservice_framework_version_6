package uk.gov.justice.services.core.lifecycle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.catchup.CatchupListener;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupCompletedEvent;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupStartedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.Shutterable;
import uk.gov.justice.services.core.lifecycle.shuttering.ShutteringListener;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ObjectShutteredEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ObjectUnshutteredEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationStateControllerTest {

    @InjectMocks
    private ApplicationStateController applicationStateController;

    @Test
    public void shouldCallCatchupRequestedForAllCatchupListeners() throws Exception {

        final Object caller_1 = new Object();
        final Object caller_2 = new Object();

        final ZonedDateTime catchupStartedTime_1 = new UtcClock().now();
        final ZonedDateTime catchupStartedTime_2 = catchupStartedTime_1.plusMinutes(23);

        final CatchupListener catchupListener_1 = mock(CatchupListener.class);
        final CatchupListener catchupListener_2 = mock(CatchupListener.class);

        applicationStateController.addCatchupListener(catchupListener_1);
        applicationStateController.addCatchupListener(catchupListener_2);

        final CatchupRequestedEvent catchupRequestedEvent_1 = new CatchupRequestedEvent(caller_1, catchupStartedTime_1);
        final CatchupRequestedEvent catchupRequestedEvent_2 = new CatchupRequestedEvent(caller_2, catchupStartedTime_2);

        applicationStateController.fireCatchupRequested(catchupRequestedEvent_1);

        verify(catchupListener_1).catchupRequested(catchupRequestedEvent_1);
        verify(catchupListener_2).catchupRequested(catchupRequestedEvent_1);

        applicationStateController.removeCatchupListener(catchupListener_2);

        applicationStateController.fireCatchupRequested(catchupRequestedEvent_2);

        verify(catchupListener_1).catchupRequested(catchupRequestedEvent_2);
        verify(catchupListener_2, never()).catchupRequested(catchupRequestedEvent_2);
    }

    @Test
    public void shouldCallCatchupStartedOnAllCatchupListeners() throws Exception {

        final ZonedDateTime catchupStartedTime_1 = new UtcClock().now();
        final ZonedDateTime catchupStartedTime_2 = catchupStartedTime_1.plusMinutes(23);

        final CatchupListener catchupListener_1 = mock(CatchupListener.class);
        final CatchupListener catchupListener_2 = mock(CatchupListener.class);

        applicationStateController.addCatchupListener(catchupListener_1);
        applicationStateController.addCatchupListener(catchupListener_2);

        final CatchupStartedEvent catchupStartedEvent_1 = new CatchupStartedEvent(catchupStartedTime_1);
        final CatchupStartedEvent catchupStartedEvent_2 = new CatchupStartedEvent(catchupStartedTime_2);

        applicationStateController.fireCatchupStarted(catchupStartedEvent_1);

        verify(catchupListener_1).catchupStarted(catchupStartedEvent_1);
        verify(catchupListener_2).catchupStarted(catchupStartedEvent_1);

        applicationStateController.removeCatchupListener(catchupListener_2);

        applicationStateController.fireCatchupStarted(catchupStartedEvent_2);

        verify(catchupListener_1).catchupStarted(catchupStartedEvent_2);
        verify(catchupListener_2, never()).catchupStarted(catchupStartedEvent_2);
    }

    @Test
    public void shouldCallCatchupCompletedOnAllCatchupListeners() throws Exception {

        final long currentEventNumber_1 = 23498L;
        final int totalNumberOfEvents_1 = 23;

        final long currentEventNumber_2 = 98273L;
        final int totalNumberOfEvents_2 = 83;

        final ZonedDateTime catchupCompletedTime_1 = new UtcClock().now();
        final ZonedDateTime catchupCompletedTime_2 = catchupCompletedTime_1.plusMinutes(23);

        final CatchupCompletedEvent catchupCompletedEvent_1 = new CatchupCompletedEvent(
                currentEventNumber_1,
                totalNumberOfEvents_1,
                catchupCompletedTime_1);
        final CatchupCompletedEvent catchupCompletedEvent_2 = new CatchupCompletedEvent(
                currentEventNumber_2,
                totalNumberOfEvents_2,
                catchupCompletedTime_2);

        final CatchupListener catchupListener_1 = mock(CatchupListener.class);
        final CatchupListener catchupListener_2 = mock(CatchupListener.class);

        applicationStateController.addCatchupListener(catchupListener_1);
        applicationStateController.addCatchupListener(catchupListener_2);

        applicationStateController.fireCatchupCompleted(catchupCompletedEvent_1);

        verify(catchupListener_1).catchupCompleted(catchupCompletedEvent_1);
        verify(catchupListener_2).catchupCompleted(catchupCompletedEvent_1);

        applicationStateController.removeCatchupListener(catchupListener_2);

        applicationStateController.fireCatchupCompleted(catchupCompletedEvent_2);

        verify(catchupListener_1).catchupCompleted(catchupCompletedEvent_2);
        verify(catchupListener_2, never()).catchupCompleted(catchupCompletedEvent_2);
    }

    @Test
    public void shouldCallShutteringRequestedOnAllShutteringListeners() throws Exception {

        final Object caller_1 = new Object();
        final Object caller_2 = new Object();

        final ZonedDateTime shutteringRequestedTime_1 = new UtcClock().now();
        final ZonedDateTime shutteringRequestedTime_2 = shutteringRequestedTime_1.plusMinutes(23);

        final ShutteringRequestedEvent shutteringRequestedEvent_1 = new ShutteringRequestedEvent(caller_1, shutteringRequestedTime_1);
        final ShutteringRequestedEvent shutteringRequestedEvent_2 = new ShutteringRequestedEvent(caller_2, shutteringRequestedTime_2);

        final ShutteringListener shutteringListener_1 = mock(ShutteringListener.class);
        final ShutteringListener shutteringListener_2 = mock(ShutteringListener.class);

        applicationStateController.addShutteringListener(shutteringListener_1);
        applicationStateController.addShutteringListener(shutteringListener_2);

        applicationStateController.fireShutteringRequested(shutteringRequestedEvent_1);

        verify(shutteringListener_1).shutteringRequested(shutteringRequestedEvent_1);
        verify(shutteringListener_2).shutteringRequested(shutteringRequestedEvent_1);

        applicationStateController.removeShutteringListener(shutteringListener_2);

        applicationStateController.fireShutteringRequested(shutteringRequestedEvent_2);

        verify(shutteringListener_1).shutteringRequested(shutteringRequestedEvent_2);
        verify(shutteringListener_2, never()).shutteringRequested(shutteringRequestedEvent_2);
    }

    @Test
    public void shouldCallObjectShutteredOnAllShutteringListeners() throws Exception {

        final Shutterable shutterable_1 = mock(Shutterable.class);
        final Shutterable shutterable_2 = mock(Shutterable.class);

        final ZonedDateTime objectShutteredTime_1 = new UtcClock().now();
        final ZonedDateTime objectShutteredTime_2 = objectShutteredTime_1.plusMinutes(23);

        final ObjectShutteredEvent objectShutteredEvent_1 = new ObjectShutteredEvent(shutterable_1, objectShutteredTime_1);
        final ObjectShutteredEvent objectShutteredEvent_2 = new ObjectShutteredEvent(shutterable_2, objectShutteredTime_2);

        final ShutteringListener shutteringListener_1 = mock(ShutteringListener.class);
        final ShutteringListener shutteringListener_2 = mock(ShutteringListener.class);

        applicationStateController.addShutteringListener(shutteringListener_1);
        applicationStateController.addShutteringListener(shutteringListener_2);

        applicationStateController.fireObjectShuttered(objectShutteredEvent_1);

        verify(shutteringListener_1).objectShuttered(objectShutteredEvent_1);
        verify(shutteringListener_2).objectShuttered(objectShutteredEvent_1);

        applicationStateController.removeShutteringListener(shutteringListener_2);

        applicationStateController.fireObjectShuttered(objectShutteredEvent_2);

        verify(shutteringListener_1).objectShuttered(objectShutteredEvent_2);
        verify(shutteringListener_2, never()).objectShuttered(objectShutteredEvent_2);
    }

    @Test
    public void shouldCallShutteringCompletedOnAllShutteringListeners() throws Exception {

        final ZonedDateTime shutteringCompleteTime_1 = new UtcClock().now();
        final ZonedDateTime shutteringCompleteTime_2 = shutteringCompleteTime_1.plusMinutes(23);

        final ShutteringCompleteEvent shutteringCompleteEvent_1 = new ShutteringCompleteEvent(shutteringCompleteTime_1);
        final ShutteringCompleteEvent shutteringCompleteEvent_2 = new ShutteringCompleteEvent(shutteringCompleteTime_2);

        final ShutteringListener shutteringListener_1 = mock(ShutteringListener.class);
        final ShutteringListener shutteringListener_2 = mock(ShutteringListener.class);

        applicationStateController.addShutteringListener(shutteringListener_1);
        applicationStateController.addShutteringListener(shutteringListener_2);

        applicationStateController.fireShutteringComplete(shutteringCompleteEvent_1);

        verify(shutteringListener_1).shutteringComplete(shutteringCompleteEvent_1);
        verify(shutteringListener_2).shutteringComplete(shutteringCompleteEvent_1);

        applicationStateController.removeShutteringListener(shutteringListener_2);

        applicationStateController.fireShutteringComplete(shutteringCompleteEvent_2);

        verify(shutteringListener_1).shutteringComplete(shutteringCompleteEvent_2);
        verify(shutteringListener_2, never()).shutteringComplete(shutteringCompleteEvent_2);
    }

    @Test
    public void shouldCallUnshutteringRequestedOnAllShutteringListeners() throws Exception {

        final Object caller_1 = new Object();
        final Object caller_2 = new Object();

        final ZonedDateTime unshutteringRequestedTime_1 = new UtcClock().now();
        final ZonedDateTime unshutteringRequestedTime_2 = unshutteringRequestedTime_1.plusMinutes(23);

        final UnshutteringRequestedEvent unshutteringRequestedEvent_1 = new UnshutteringRequestedEvent(caller_1, unshutteringRequestedTime_1);
        final UnshutteringRequestedEvent unshutteringRequestedEvent_2 = new UnshutteringRequestedEvent(caller_2, unshutteringRequestedTime_2);

        final ShutteringListener shutteringListener_1 = mock(ShutteringListener.class);
        final ShutteringListener shutteringListener_2 = mock(ShutteringListener.class);

        applicationStateController.addShutteringListener(shutteringListener_1);
        applicationStateController.addShutteringListener(shutteringListener_2);

        applicationStateController.fireUnshutteringRequested(unshutteringRequestedEvent_1);

        verify(shutteringListener_1).unshutteringRequested(unshutteringRequestedEvent_1);
        verify(shutteringListener_2).unshutteringRequested(unshutteringRequestedEvent_1);

        applicationStateController.removeShutteringListener(shutteringListener_2);

        applicationStateController.fireUnshutteringRequested(unshutteringRequestedEvent_2);

        verify(shutteringListener_1).unshutteringRequested(unshutteringRequestedEvent_2);
        verify(shutteringListener_2, never()).unshutteringRequested(unshutteringRequestedEvent_2);
    }

    @Test
    public void shouldCallObjectUnshutteredOnAllShutteringListeners() throws Exception {

        final Shutterable shutterable_1 = mock(Shutterable.class);
        final Shutterable shutterable_2 = mock(Shutterable.class);

        final ZonedDateTime objectUnshutteredTime_1 = new UtcClock().now();
        final ZonedDateTime objectUnshutteredTime_2 = objectUnshutteredTime_1.plusMinutes(23);

        final ObjectUnshutteredEvent objectUnshutteredEvent_1 = new ObjectUnshutteredEvent(shutterable_1, objectUnshutteredTime_1);
        final ObjectUnshutteredEvent objectUnshutteredEvent_2 = new ObjectUnshutteredEvent(shutterable_2, objectUnshutteredTime_2);

        final ShutteringListener shutteringListener_1 = mock(ShutteringListener.class);
        final ShutteringListener shutteringListener_2 = mock(ShutteringListener.class);

        applicationStateController.addShutteringListener(shutteringListener_1);
        applicationStateController.addShutteringListener(shutteringListener_2);

        applicationStateController.fireObjectUnshuttered(objectUnshutteredEvent_1);

        verify(shutteringListener_1).objectUnshuttered(objectUnshutteredEvent_1);
        verify(shutteringListener_2).objectUnshuttered(objectUnshutteredEvent_1);

        applicationStateController.removeShutteringListener(shutteringListener_2);

        applicationStateController.fireObjectUnshuttered(objectUnshutteredEvent_2);

        verify(shutteringListener_1).objectUnshuttered(objectUnshutteredEvent_2);
        verify(shutteringListener_2, never()).objectUnshuttered(objectUnshutteredEvent_2);
    }

    @Test
    public void shouldCallUnshutteringCompletedOnAllShutteringListeners() throws Exception {

        final ZonedDateTime unshutteringCompleteTime_1 = new UtcClock().now();
        final ZonedDateTime unshutteringCompleteTime_2 = unshutteringCompleteTime_1.plusMinutes(23);

        final UnshutteringCompleteEvent unshutteringCompleteEvent_1 = new UnshutteringCompleteEvent(unshutteringCompleteTime_1);
        final UnshutteringCompleteEvent unshutteringCompleteEvent_2 = new UnshutteringCompleteEvent(unshutteringCompleteTime_2);

        final ShutteringListener shutteringListener_1 = mock(ShutteringListener.class);
        final ShutteringListener shutteringListener_2 = mock(ShutteringListener.class);

        applicationStateController.addShutteringListener(shutteringListener_1);
        applicationStateController.addShutteringListener(shutteringListener_2);

        applicationStateController.fireUnshutteringComplete(unshutteringCompleteEvent_1);

        verify(shutteringListener_1).unshutteringComplete(unshutteringCompleteEvent_1);
        verify(shutteringListener_2).unshutteringComplete(unshutteringCompleteEvent_1);

        applicationStateController.removeShutteringListener(shutteringListener_2);

        applicationStateController.fireUnshutteringComplete(unshutteringCompleteEvent_2);

        verify(shutteringListener_1).unshutteringComplete(unshutteringCompleteEvent_2);
        verify(shutteringListener_2, never()).unshutteringComplete(unshutteringCompleteEvent_2);
    }
}
