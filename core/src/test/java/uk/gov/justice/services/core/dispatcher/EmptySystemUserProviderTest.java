package uk.gov.justice.services.core.dispatcher;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EmptySystemUserProviderTest {

    @Mock
    private Logger logger;

    @InjectMocks
    final SystemUserProvider provider = new EmptySystemUserProvider();


    @Test
    public void shouldReturnEmptyOptional() throws Exception {
        assertThat(provider.getContextSystemUserId(), is(Optional.empty()));
    }

    @Test
    public void shouldLogError() {
        provider.getContextSystemUserId();
        verify(logger).error("Could not fetch system user. system-users-library not available in the classpath");
    }

}