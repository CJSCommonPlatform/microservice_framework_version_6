package uk.gov.justice.services.management.suspension.process;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.extension.util.TestBean.of;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.suspension.api.Suspendable;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SuspendablesProviderTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @Mock
    private BeanManager beanManager;

    @InjectMocks
    private SuspendablesProvider suspendablesProvider;

    @Test
    public void shouldFindAllInstancesOfShutteringExecutorInCDI() throws Exception {

        final Bean<Suspendable_1> shutteringExecutorBean_1 = of(Suspendable_1.class);
        final Bean<Suspendable_2> shutteringExecutorBean_2 = of(Suspendable_2.class);
        final Bean<Suspendable_3> shutteringExecutorBean_3 = of(Suspendable_3.class);

        final Suspendable_1 shutteringExecutor_1 = new Suspendable_1();
        final Suspendable_2 shutteringExecutor_2 = new Suspendable_2();
        final Suspendable_3 shutteringExecutor_3 = new Suspendable_3();

        final Set<Bean<?>> beans = newHashSet(
                shutteringExecutorBean_1,
                shutteringExecutorBean_2,
                shutteringExecutorBean_3);

        when(beanManager.getBeans(Suspendable.class)).thenReturn(beans);
        when(cdiInstanceResolver.getInstanceOf(Suspendable_1.class, beanManager)).thenReturn(shutteringExecutor_1);
        when(cdiInstanceResolver.getInstanceOf(Suspendable_2.class, beanManager)).thenReturn(shutteringExecutor_2);
        when(cdiInstanceResolver.getInstanceOf(Suspendable_3.class, beanManager)).thenReturn(shutteringExecutor_3);

        final List<Suspendable> suspendables = suspendablesProvider.getSuspendables();

        assertThat(suspendables.size(), is(3));

        assertThat(suspendables, hasItem(shutteringExecutor_1));
        assertThat(suspendables, hasItem(shutteringExecutor_2));
        assertThat(suspendables, hasItem(shutteringExecutor_3));
    }
}

class Suspendable_1 implements Suspendable {}

class Suspendable_2 implements Suspendable {}

class Suspendable_3 implements Suspendable {}
