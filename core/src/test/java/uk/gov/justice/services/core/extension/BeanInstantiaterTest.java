package uk.gov.justice.services.core.extension;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BeanInstantiaterTest {

    @Mock
    BeanManager beanManager;

    @InjectMocks
    BeanInstantiater beanInstantiater;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInstantiateABean() throws Exception {

        final Bean<Object> bean = mock(Bean.class);
        final Context context = mock(Context.class);
        final CreationalContext<Object> creationalContext = mock(CreationalContext.class);
        final Object instance = mock(Object.class);

        when(beanManager.getContext(bean.getScope())).thenReturn(context);
        when(beanManager.createCreationalContext(bean)).thenReturn(creationalContext);
        when(context.get(bean, creationalContext)).thenReturn(instance);

        final Object result = beanInstantiater.instantiate(bean);
        assertThat(result, is(instance));
    }

}