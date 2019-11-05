package uk.gov.justice.services.jmx.bootstrap.blacklist;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

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
public class BlacklistedCommandsScannerTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private BlacklistedCommandsScanner blacklistedCommandsScanner;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetTheUniqueListOfBlackListedSystemCommandsByInstantiatingBlacklistedCommandsObjects() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);
        final Bean blacklistedCommandsBean_1 = mock(Bean.class);
        final Bean blacklistedCommandsBean_2 = mock(Bean.class);

        final Set<Bean<?>> cdiBeans = newHashSet(bean_1, blacklistedCommandsBean_1, bean_2, blacklistedCommandsBean_2);

        when(bean_1.getBeanClass()).thenReturn(Object.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        when(blacklistedCommandsBean_1.getBeanClass()).thenReturn(BlacklistedCommands_1.class);
        when(blacklistedCommandsBean_2.getBeanClass()).thenReturn(BlacklistedCommands_2.class);

        when(cdiInstanceResolver.getInstanceOf(BlacklistedCommands_1.class, beanManager)).thenReturn(new BlacklistedCommands_1());
        when(cdiInstanceResolver.getInstanceOf(BlacklistedCommands_2.class, beanManager)).thenReturn(new BlacklistedCommands_2());

        final Set<SystemCommand> blacklistedCommands = blacklistedCommandsScanner.scanForBlacklistedCommands(cdiBeans, beanManager);

        assertThat(blacklistedCommands.size(), is(3));
        assertThat(blacklistedCommands, hasItem(new SystemCommand_1()));
        assertThat(blacklistedCommands, hasItem(new SystemCommand_2()));
        assertThat(blacklistedCommands, hasItem(new SystemCommand_3()));
    }
}

class BlacklistedCommands_1 implements BlacklistedCommands {

    @Override
    public List<SystemCommand> getBlackListedCommands() {
        return asList(
                new SystemCommand_1(),
                new SystemCommand_2()
        );
    }
}

class BlacklistedCommands_2 implements BlacklistedCommands {

    @Override
    public List<SystemCommand> getBlackListedCommands() {
        return asList(
                new SystemCommand_2(),
                new SystemCommand_3()
        );
    }
}
