package uk.gov.justice.services.test.utils.common.host;

import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.INTEGRATION_HOST_KEY;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
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

        Assert.assertThat(getHost(), CoreMatchers.is("localhost"));
    }

    @Test
    public void shouldGetHostSetAsSystemProperty() throws Exception {

        final String host = "my-host.com";

        System.setProperty(INTEGRATION_HOST_KEY, host);

        Assert.assertThat(getHost(), CoreMatchers.is(host));
    }
}
