package uk.gov.justice.services.jmx.bootstrap;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.bootstrap.blacklist.BlacklistedCommandsFilter;
import uk.gov.justice.services.jmx.command.HandlerMethodValidator;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class HandlesSystemCommandProxyResolverTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @Mock
    private SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory;

    @Mock
    private HandlerMethodValidator handlerMethodValidator;

    @Mock
    private BlacklistedCommandsFilter blacklistedCommandsFilter;

    @InjectMocks
    private SystemCommandProxyResolver systemCommandProxyResolver;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetProxiesForAllTheAnnotatedHanderMethods() throws Exception {
        final Bean<?> bean = mock(Bean.class);
        final BeanManager beanManager = mock(BeanManager.class);

        final Object systemCommandHandler = new HandlesSystemCommandHandler();

        final Class beanClass = systemCommandHandler.getClass();

        final Method handlerMethod_1 = beanClass.getDeclaredMethod("runSystemCommand_1");
        final Method handlerMethod_2 = beanClass.getDeclaredMethod("runSystemCommand_2");
        final Method handlerMethod_3 = beanClass.getDeclaredMethod("runSystemCommand_3");

        final SystemCommandHandlerProxy systemCommandHandlerProxy_1 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_2 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_3 = mock(SystemCommandHandlerProxy.class);

        final Set<SystemCommand> blacklistedCommands = newHashSet(mock(SystemCommand.class));

        when(bean.getBeanClass()).thenReturn(beanClass);
        when(cdiInstanceResolver.getInstanceOf(beanClass, beanManager)).thenReturn(systemCommandHandler);

        when(blacklistedCommandsFilter.isSystemCommandAllowed(HandlesSystemSystemCommand_1.SYSTEM_COMMAND_NAME_1, blacklistedCommands)).thenReturn(true);
        when(systemCommandHandlerProxyFactory.create(
                HandlesSystemSystemCommand_1.SYSTEM_COMMAND_NAME_1,
                handlerMethod_1,
                systemCommandHandler,
                handlerMethodValidator
        )).thenReturn(systemCommandHandlerProxy_1);

        when(blacklistedCommandsFilter.isSystemCommandAllowed(HandlesSystemSystemCommand_2.SYSTEM_COMMAND_NAME_2, blacklistedCommands)).thenReturn(true);
        when(systemCommandHandlerProxyFactory.create(
                HandlesSystemSystemCommand_2.SYSTEM_COMMAND_NAME_2,
                handlerMethod_2,
                systemCommandHandler,
                handlerMethodValidator
        )).thenReturn(systemCommandHandlerProxy_2);

        when(blacklistedCommandsFilter.isSystemCommandAllowed(HandlesSystemSystemCommand_3.SYSTEM_COMMAND_NAME_3, blacklistedCommands)).thenReturn(true);
        when(systemCommandHandlerProxyFactory.create(
                HandlesSystemSystemCommand_3.SYSTEM_COMMAND_NAME_3,
                handlerMethod_3,
                systemCommandHandler,
                handlerMethodValidator
        )).thenReturn(systemCommandHandlerProxy_3);

        final List<SystemCommandHandlerProxy> systemCommandHandlerProxies = systemCommandProxyResolver.allCommandProxiesFor(bean, beanManager, blacklistedCommands);

        assertThat(systemCommandHandlerProxies.size(), is(3));

        assertThat(systemCommandHandlerProxies, hasItem(systemCommandHandlerProxy_1));
        assertThat(systemCommandHandlerProxies, hasItem(systemCommandHandlerProxy_2));
        assertThat(systemCommandHandlerProxies, hasItem(systemCommandHandlerProxy_3));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotCreateProxyIfTheCommandNameIsBlacklisted() throws Exception {
        final Bean<?> bean = mock(Bean.class);
        final BeanManager beanManager = mock(BeanManager.class);

        final Object systemCommandHandler = new HandlesSystemCommandHandler();

        final Class beanClass = systemCommandHandler.getClass();

        final Method handlerMethod_1 = beanClass.getDeclaredMethod("runSystemCommand_1");
        final Method handlerMethod_2 = beanClass.getDeclaredMethod("runSystemCommand_2");
        final Method handlerMethod_3 = beanClass.getDeclaredMethod("runSystemCommand_3");

        final SystemCommandHandlerProxy systemCommandHandlerProxy_1 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_2 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_3 = mock(SystemCommandHandlerProxy.class);

        final Set<SystemCommand> blacklistedCommands = newHashSet(mock(SystemCommand.class));

        when(bean.getBeanClass()).thenReturn(beanClass);
        when(cdiInstanceResolver.getInstanceOf(beanClass, beanManager)).thenReturn(systemCommandHandler);

        when(blacklistedCommandsFilter.isSystemCommandAllowed(HandlesSystemSystemCommand_1.SYSTEM_COMMAND_NAME_1, blacklistedCommands)).thenReturn(true);
        when(systemCommandHandlerProxyFactory.create(
                HandlesSystemSystemCommand_1.SYSTEM_COMMAND_NAME_1,
                handlerMethod_1,
                systemCommandHandler,
                handlerMethodValidator
        )).thenReturn(systemCommandHandlerProxy_1);

        when(blacklistedCommandsFilter.isSystemCommandAllowed(HandlesSystemSystemCommand_2.SYSTEM_COMMAND_NAME_2, blacklistedCommands)).thenReturn(false);

        when(blacklistedCommandsFilter.isSystemCommandAllowed(HandlesSystemSystemCommand_3.SYSTEM_COMMAND_NAME_3, blacklistedCommands)).thenReturn(true);
        when(systemCommandHandlerProxyFactory.create(
                HandlesSystemSystemCommand_3.SYSTEM_COMMAND_NAME_3,
                handlerMethod_3,
                systemCommandHandler,
                handlerMethodValidator
        )).thenReturn(systemCommandHandlerProxy_3);

        final List<SystemCommandHandlerProxy> systemCommandHandlerProxies = systemCommandProxyResolver.allCommandProxiesFor(bean, beanManager, blacklistedCommands);

        assertThat(systemCommandHandlerProxies.size(), is(2));

        assertThat(systemCommandHandlerProxies, hasItem(systemCommandHandlerProxy_1));
        assertThat(systemCommandHandlerProxies, hasItem(systemCommandHandlerProxy_3));
    }

    private static class HandlesSystemCommandHandler {

        @HandlesSystemCommand(HandlesSystemSystemCommand_1.SYSTEM_COMMAND_NAME_1)
        public void runSystemCommand_1() {
            
        }
        @HandlesSystemCommand(HandlesSystemSystemCommand_2.SYSTEM_COMMAND_NAME_2)
        public void runSystemCommand_2() {

        }

        @HandlesSystemCommand(HandlesSystemSystemCommand_3.SYSTEM_COMMAND_NAME_3)
        public void runSystemCommand_3() {

        }

        public void someOtherMethod() {
            
        }
    }

    private static class HandlesSystemSystemCommand_1 implements SystemCommand {

        public static final String SYSTEM_COMMAND_NAME_1 = "SYSTEM_COMMAND_NAME_1";

        @Override
        public String getName() {
            return SYSTEM_COMMAND_NAME_1;
        }

        @Override
        public String getDescription() {
            return "description 1";
        }
    }

    private static class HandlesSystemSystemCommand_2 implements SystemCommand {

        private static final String SYSTEM_COMMAND_NAME_2 = "SYSTEM_COMMAND_NAME_2";

        @Override
        public String getName() {
            return SYSTEM_COMMAND_NAME_2;
        }

        @Override
        public String getDescription() {
            return "description 2";
        }
    }

    private static class HandlesSystemSystemCommand_3 implements SystemCommand {

        private static final String SYSTEM_COMMAND_NAME_3 = "SYSTEM_COMMAND_NAME_3";

        @Override
        public String getName() {
            return SYSTEM_COMMAND_NAME_3;
        }

        @Override
        public String getDescription() {
            return "description 3";
        }
    }
}
