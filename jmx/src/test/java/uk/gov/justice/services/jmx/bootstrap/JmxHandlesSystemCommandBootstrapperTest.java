package uk.gov.justice.services.jmx.bootstrap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class JmxHandlesSystemCommandBootstrapperTest {

    @Mock
    private ObjectFactory objectFactory;

    @InjectMocks
    private JmxSystemCommandBootstrapper jmxSystemCommandBootstrapper;

    @Test
    public void shouldBootstrapTheJmxCommandBeanAndHandlers() throws Exception {

        final SystemCommandScanner systemCommandScanner = mock(SystemCommandScanner.class);
        final BeanManager beanManager = mock(BeanManager.class);

        when(objectFactory.systemCommandScanner()).thenReturn(systemCommandScanner);

        jmxSystemCommandBootstrapper.afterDeploymentValidation(mock(AfterDeploymentValidation.class), beanManager);

        verify(systemCommandScanner).registerSystemCommands(beanManager);
    }

    @Test
    public void shouldConstructItselfWithAnObjectFactory() throws Exception {

        assertThat(getValueOfField(new JmxSystemCommandBootstrapper(), "objectFactory", ObjectFactory.class), is(notNullValue()));
    }
}
