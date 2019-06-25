package uk.gov.justice.services.jmx.bootstrap;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;
import uk.gov.justice.services.jmx.command.SystemCommandStore;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HandlesSystemCommandScannerTest {

    @Mock
    private SystemCommandProxyResolver systemCommandProxyResolver;

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private SystemCommandScanner systemCommandScanner;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRegisterSystemCommands() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final SystemCommandHandlerProxy systemCommandHandlerProxy_1_1 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_1_2 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_2_1 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_2_2 = mock(SystemCommandHandlerProxy.class);

        final List<SystemCommandHandlerProxy> beanProxies_1 = asList(systemCommandHandlerProxy_1_1, systemCommandHandlerProxy_1_2);
        final List<SystemCommandHandlerProxy> beanProxies_2 = asList(systemCommandHandlerProxy_2_1, systemCommandHandlerProxy_2_2);

        final Set<Bean<?>> cdiBeans = newHashSet(bean_1, bean_2);

        final SystemCommandStore systemCommandStore = mock(SystemCommandStore.class);

        when(beanManager.getBeans(Object.class)).thenReturn(cdiBeans);

        when(systemCommandProxyResolver.allCommandProxiesFor(bean_1, beanManager)).thenReturn(beanProxies_1);
        when(systemCommandProxyResolver.allCommandProxiesFor(bean_2, beanManager)).thenReturn(beanProxies_2);
        when(cdiInstanceResolver.getInstanceOf(SystemCommandStore.class, beanManager)).thenReturn(systemCommandStore);

        systemCommandScanner.registerSystemCommands(beanManager);

        final ArgumentCaptor<List> captor = forClass(List.class);

        verify(systemCommandStore).store(captor.capture());

        final List<SystemCommandHandlerProxy> proxies = captor.getValue();

        assertThat(proxies.size(), is(4));
        assertThat(proxies, hasItem(systemCommandHandlerProxy_1_1));
        assertThat(proxies, hasItem(systemCommandHandlerProxy_1_2));
        assertThat(proxies, hasItem(systemCommandHandlerProxy_2_1));
        assertThat(proxies, hasItem(systemCommandHandlerProxy_2_2));
    }
}

