package uk.gov.justice.services.management.shuttering.process;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.extension.util.TestBean.of;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;

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
public class ShutteringExecutorProviderTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @Mock
    private BeanManager beanManager;

    @InjectMocks
    private ShutteringExecutorProvider shutteringExecutorProvider;

    @Test
    public void shouldFindAllInstancesOfShutteringExecutorInCDI() throws Exception {

        final Bean<ShutteringExecutor_1> shutteringExecutorBean_1 = of(ShutteringExecutor_1.class);
        final Bean<ShutteringExecutor_2> shutteringExecutorBean_2 = of(ShutteringExecutor_2.class);
        final Bean<ShutteringExecutor_3> shutteringExecutorBean_3 = of(ShutteringExecutor_3.class);

        final ShutteringExecutor_1 shutteringExecutor_1 = new ShutteringExecutor_1();
        final ShutteringExecutor_2 shutteringExecutor_2 = new ShutteringExecutor_2();
        final ShutteringExecutor_3 shutteringExecutor_3 = new ShutteringExecutor_3();

        final Set<Bean<?>> beans = newHashSet(
                shutteringExecutorBean_1,
                shutteringExecutorBean_2,
                shutteringExecutorBean_3);

        when(beanManager.getBeans(ShutteringExecutor.class)).thenReturn(beans);
        when(cdiInstanceResolver.getInstanceOf(ShutteringExecutor_1.class, beanManager)).thenReturn(shutteringExecutor_1);
        when(cdiInstanceResolver.getInstanceOf(ShutteringExecutor_2.class, beanManager)).thenReturn(shutteringExecutor_2);
        when(cdiInstanceResolver.getInstanceOf(ShutteringExecutor_3.class, beanManager)).thenReturn(shutteringExecutor_3);

        final List<ShutteringExecutor> shutteringExecutors = shutteringExecutorProvider.getShutteringExecutors();

        assertThat(shutteringExecutors.size(), is(3));

        assertThat(shutteringExecutors, hasItem(shutteringExecutor_1));
        assertThat(shutteringExecutors, hasItem(shutteringExecutor_2));
        assertThat(shutteringExecutors, hasItem(shutteringExecutor_3));
    }
}

class ShutteringExecutor_1 implements ShutteringExecutor {}

class ShutteringExecutor_2 implements ShutteringExecutor {}

class ShutteringExecutor_3 implements ShutteringExecutor {}
