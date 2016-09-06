package uk.gov.justice.services.test.utils.common.host;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.INTEGRATION_HOST_KEY;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TestHostProviderTest {


    @InjectMocks
    private TestHostProvider testHostProvider;

    @Before
    @After
    public void clearSystemProperty() {
        System.clearProperty(INTEGRATION_HOST_KEY);
    }

    @Test
    public void shouldGetLocalhostByDefault() throws Exception {

        assertThat(getHost(), is("localhost"));
    }

    @Test
    public void shouldGetHostSetAsSystemProperty() throws Exception {

        final String host = "my-host.com";

        System.setProperty(INTEGRATION_HOST_KEY, host);

        assertThat(getHost(), is(host));
    }
}
