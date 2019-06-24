package uk.gov.justice.services.management.shuttering.startup;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.management.shuttering.observers.unshuttering.UnshutteringRegistry;

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
public class UnshutteringExecutorScannerTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private UnshutteringExecutorScanner unshutteringExecutorScanner;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFindAllUnshutteringExecutorsFromCdiAndRegister() throws Exception {

        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);
        final BeanManager beanManager = mock(BeanManager.class);
        final UnshutteringRegistry unshutteringRegistry = mock(UnshutteringRegistry.class);

        when(cdiInstanceResolver.getInstanceOf(
                UnshutteringRegistry.class,
                beanManager)).thenReturn(unshutteringRegistry);

        final Bean unshutteringExecutorBean_1 = mock(Bean.class);
        final Bean unshutteringExecutorBean_2 = mock(Bean.class);
        final Bean notUnshutteringExecutorBean = mock(Bean.class);

        final Set<Bean<?>> beans = newHashSet(
                unshutteringExecutorBean_1,
                notUnshutteringExecutorBean,
                unshutteringExecutorBean_2);

        when(beanManager.getBeans(Object.class)).thenReturn(beans);
        when(unshutteringExecutorBean_1.getBeanClass()).thenReturn(UnshutteringExecutor_1.class);
        when(unshutteringExecutorBean_2.getBeanClass()).thenReturn(UnshutteringExecutor_2.class);
        when(notUnshutteringExecutorBean.getBeanClass()).thenReturn(NotUnshutteringExecutor.class);

        unshutteringExecutorScanner.afterDeploymentValidation(event, beanManager);

        verify(unshutteringRegistry).registerAsUnshutterable(UnshutteringExecutor_1.class);
        verify(unshutteringRegistry).registerAsUnshutterable(UnshutteringExecutor_2.class);

        verify(unshutteringRegistry, never()).registerAsUnshutterable(NotShutteringExecutor.class);
    }
}

@UnshutteringExecutor
class UnshutteringExecutor_1 {}

@UnshutteringExecutor
class UnshutteringExecutor_2 {}

class NotUnshutteringExecutor {}


