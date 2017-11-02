package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.REMOTE;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWithMemberAsFirstMethodOf;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherCacheTest {

    private InjectionPoint adaptorCommandApiInjectionPointA = injectionPointWithMemberAsFirstMethodOf(TestCommandApiAdaptorA.class);

    private InjectionPoint adaptorCommandApiInjectionPointB = injectionPointWithMemberAsFirstMethodOf(TestCommandApiAdaptorB.class);

    private InjectionPoint adaptorQueryApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(TestQueryApiAdaptor.class);

    private DispatcherCache dispatcherCache = new DispatcherCache();

    @Test
    public void shouldReturnTheSameDispatcherForTwoInjectionPoints() throws Exception {

        Dispatcher resultA = dispatcherCache.dispatcherFor(adaptorCommandApiInjectionPointA);
        Dispatcher resultB = dispatcherCache.dispatcherFor(adaptorCommandApiInjectionPointB);
        assertThat(resultA, is(sameInstance(resultB)));
    }

    @Test
    public void shouldReturnDifferentDispatchersForCommandApiAndQueryApiInjectionPoints() throws Exception {

        Dispatcher resultA = dispatcherCache.dispatcherFor(adaptorCommandApiInjectionPointA);
        Dispatcher resultB = dispatcherCache.dispatcherFor(adaptorQueryApiInjectionPoint);
        assertThat(resultA, is(not(sameInstance(resultB))));
    }

    @Test
    public void shouldReturnTheSameDispatcherForTwoEventsConcerningSameComponent() throws Exception {

        final ServiceComponentFoundEvent foundEvent = new ServiceComponentFoundEvent("COMMAND_API", null, LOCAL);
        final ServiceComponentFoundEvent foundEvent2 = new ServiceComponentFoundEvent("COMMAND_API", null, LOCAL);

        final Dispatcher dispatcher1 = dispatcherCache.dispatcherFor(foundEvent);
        final Dispatcher dispatcher2 = dispatcherCache.dispatcherFor(foundEvent2);
        assertThat(dispatcher1, is(sameInstance(dispatcher2)));
    }

    @Test
    public void shouldReturnDifferentDispatchersForLocalAndRemoteEvent() throws Exception {

        final ServiceComponentFoundEvent foundEvent = new ServiceComponentFoundEvent("COMMAND_API", null, LOCAL);
        final ServiceComponentFoundEvent foundEvent2 = new ServiceComponentFoundEvent("COMMAND_API", null, REMOTE);

        final Dispatcher dispatcher1 = dispatcherCache.dispatcherFor(foundEvent);
        final Dispatcher dispatcher2 = dispatcherCache.dispatcherFor(foundEvent2);
        assertThat(dispatcher1, is(not(sameInstance(dispatcher2))));
    }

    @Test
    public void shouldReturnDifferentDispatchersForDifferentComponents() throws Exception {

        final ServiceComponentFoundEvent foundEvent = new ServiceComponentFoundEvent("COMMAND_API", null, LOCAL);
        final ServiceComponentFoundEvent foundEvent2 = new ServiceComponentFoundEvent("QUERY_API", null, LOCAL);

        final Dispatcher dispatcher1 = dispatcherCache.dispatcherFor(foundEvent);
        final Dispatcher dispatcher2 = dispatcherCache.dispatcherFor(foundEvent2);
        assertThat(dispatcher1, is(not(sameInstance(dispatcher2))));
    }

    @Test
    public void shouldFindDispatchersByComponentAndLocation() throws Exception {

        final Dispatcher dispatcher1 = dispatcherCache.dispatcherFor(COMMAND_API, LOCAL);
        final Dispatcher dispatcher2 = dispatcherCache.dispatcherFor(COMMAND_API, LOCAL);
        assertThat(dispatcher1, is(notNullValue()));
        assertThat(dispatcher1, is(sameInstance(dispatcher2)));
    }

    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptorA {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }

    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptorB {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }

    @Adapter(QUERY_API)
    public static class TestQueryApiAdaptor {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }
}
