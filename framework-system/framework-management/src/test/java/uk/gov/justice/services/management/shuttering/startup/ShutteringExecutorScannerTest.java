package uk.gov.justice.services.management.shuttering.startup;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry;

import java.util.Set;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringExecutorScannerTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private ShutteringExecutorScanner shutteringExecutorScanner;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFindAllShutteringExecutorsFromCdiAndRegister() throws Exception {

        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);
        final BeanManager beanManager = mock(BeanManager.class);
        final ShutteringRegistry shutteringRegistry = mock(ShutteringRegistry.class);

        when(cdiInstanceResolver.getInstanceOf(
                ShutteringRegistry.class,
                beanManager)).thenReturn(shutteringRegistry);

        final Bean shutteringExecutorBean_1 = mock(Bean.class);
        final Bean shutteringExecutorBean_2 = mock(Bean.class);
        final Bean notShutteringExecutorBean = mock(Bean.class);

        final Set<Bean<?>> beans = newHashSet(
                shutteringExecutorBean_1,
                notShutteringExecutorBean,
                shutteringExecutorBean_2);

        when(beanManager.getBeans(Object.class)).thenReturn(beans);
        when(shutteringExecutorBean_1.getBeanClass()).thenReturn(ShutteringExecutor_1.class);
        when(shutteringExecutorBean_2.getBeanClass()).thenReturn(ShutteringExecutor_2.class);
        when(notShutteringExecutorBean.getBeanClass()).thenReturn(NotShutteringExecutor.class);

        shutteringExecutorScanner.afterDeploymentValidation(event, beanManager);

        verify(shutteringRegistry).registerAsShutterable(ShutteringExecutor_1.class);
        verify(shutteringRegistry).registerAsShutterable(ShutteringExecutor_2.class);

        verify(shutteringRegistry, never()).registerAsShutterable(NotShutteringExecutor.class);
    }
}

@ShutteringExecutor
class ShutteringExecutor_1 {}

@ShutteringExecutor
class ShutteringExecutor_2 {}

class NotShutteringExecutor {}


