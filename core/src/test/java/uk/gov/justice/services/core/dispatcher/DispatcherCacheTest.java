package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.lang.reflect.Member;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherCacheTest {

    @Mock
    InjectionPoint commandApiInjectionPointA;

    @Mock
    InjectionPoint commandApiInjectionPointB;

    @Mock
    Member commandApiMember;

    @Mock
    private Bean<Object> bean;

    @Test
    public void shouldCreateDispatcherForInjectionPoint() throws Exception {
        when(commandApiInjectionPointA.getMember()).thenReturn(commandApiMember);
        doReturn(DispatcherCacheTest.TestCommandApiAdaptorA.class).when(commandApiMember).getDeclaringClass();

        DispatcherCache dispatcherCache = new DispatcherCache();
        Dispatcher result = dispatcherCache.dispatcherFor(commandApiInjectionPointA);
        assertThat(result, notNullValue());
    }

    @Test
    public void shouldReturnTheSameDispatcherForTwoInjectionPoints() throws Exception {
        when(commandApiInjectionPointA.getMember()).thenReturn(commandApiMember);
        doReturn(DispatcherCacheTest.TestCommandApiAdaptorA.class).when(commandApiMember).getDeclaringClass();
        when(commandApiInjectionPointB.getMember()).thenReturn(commandApiMember);
        doReturn(DispatcherCacheTest.TestCommandApiAdaptorB.class).when(commandApiMember).getDeclaringClass();

        DispatcherCache dispatcherCache = new DispatcherCache();
        Dispatcher resultA = dispatcherCache.dispatcherFor(commandApiInjectionPointA);
        Dispatcher resultB = dispatcherCache.dispatcherFor(commandApiInjectionPointB);
        assertThat(resultA, is(resultB));
    }

    @Test
    public void shouldCreateDispatcherForEvent() throws Exception {
        final ServiceComponentFoundEvent foundEvent = new ServiceComponentFoundEvent(COMMAND_API, bean, LOCAL);

        DispatcherCache dispatcherCache = new DispatcherCache();
        Dispatcher result = dispatcherCache.dispatcherFor(foundEvent);
        assertThat(result, notNullValue());
    }

    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptorA {
        @Inject
        AsynchronousDispatcher asyncDispatcher;
    }

    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptorB {
        @Inject
        AsynchronousDispatcher asyncDispatcher;
    }

}