package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.REMOTE;

import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.util.TestInjectionPoint;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherCacheTest {

    private InjectionPoint adaptorCommandApiInjectionPointA = new TestInjectionPoint(TestCommandApiAdaptorA.class);


    private InjectionPoint adaptorCommandApiInjectionPointB = new TestInjectionPoint(TestCommandApiAdaptorB.class);

    private InjectionPoint adaptorQueryApiInjectionPoint = new TestInjectionPoint(TestQueryApiAdaptor.class);

    private DispatcherCache dispatcherCache = new DispatcherCache();

    @Before
    public void setUp() throws Exception {
        dispatcherCache = new DispatcherCache();
        dispatcherCache.dispatcherFactory = new DispatcherFactory();
        dispatcherCache.dispatcherFactory.accessControlService = mock(AccessControlService.class);
    }

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


    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptorA {
        @Inject
        AsynchronousDispatcher asyncDispatcher;

        public void dummyMethod() {

        }
    }

    @Adapter(COMMAND_API)
    public static class TestCommandApiAdaptorB {
        @Inject
        AsynchronousDispatcher asyncDispatcher;

        public void dummyMethod() {

        }
    }

    @Adapter(QUERY_API)
    public static class TestQueryApiAdaptor {
        @Inject
        AsynchronousDispatcher asyncDispatcher;

        public void dummyMethod() {

        }
    }

    @FrameworkComponent("componentNameABC")
    public static class TestCommandApiFrameworkComponentA {
        @Inject
        AsynchronousDispatcher asyncDispatcher;

        public void dummyMethod() {

        }
    }
}
