package uk.gov.justice.services.jmx.command;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.framework.utilities.cdi.CdiProvider;
import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandHandlerScannerTest {

    @Mock
    private CdiProvider cdiProvider;

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private SystemCommandScanner systemCommandScanner;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetAllSystemCommandsFromCdi() throws Exception {

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final SystemCommand_1 systemCommand_1 = new SystemCommand_1();
        final SystemCommand_2 systemCommand_2 = new SystemCommand_2();

        final Set<Bean<?>> beans = newHashSet(bean_1, bean_2);

        final CDI<Object> cdi = mock(CDI.class);
        final BeanManager beanManager = mock(BeanManager.class);

        when(cdiProvider.getCdi()).thenReturn(cdi);
        when(cdi.getBeanManager()).thenReturn(beanManager);
        when(beanManager.getBeans(SystemCommand.class)).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(SystemCommand_1.class);
        when(bean_2.getBeanClass()).thenReturn(SystemCommand_2.class);
        when(cdiInstanceResolver.getInstanceOf(SystemCommand_1.class, beanManager)).thenReturn(systemCommand_1);
        when(cdiInstanceResolver.getInstanceOf(SystemCommand_2.class, beanManager)).thenReturn(systemCommand_2);

        final List<SystemCommand> commands = systemCommandScanner.findCommands();

        assertThat(commands.size(), is(2));
        assertThat(commands, hasItem(systemCommand_1));
        assertThat(commands, hasItem(systemCommand_2));
    }
}

class SystemCommand_1 extends BaseSystemCommand {
    public SystemCommand_1() {
        super("SystemCommand_1", "description_1");
    }
}

class SystemCommand_2 extends BaseSystemCommand {
    public SystemCommand_2() {
        super("SystemCommand_2", "description_2");
    }
}
