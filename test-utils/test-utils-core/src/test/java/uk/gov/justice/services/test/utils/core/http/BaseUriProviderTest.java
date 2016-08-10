package uk.gov.justice.services.test.utils.core.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.INTEGRATION_HOST_KEY;

import org.junit.After;
import org.junit.Test;

public class BaseUriProviderTest {

    private BaseUriProvider baseUriProvider = new BaseUriProvider();

    @After
    public void resetTheSystemPropertySetInTheTest() {
        System.clearProperty(INTEGRATION_HOST_KEY);
    }

    @Test
    public void shouldGetTheBaseUriWithLocalhostAsHostnameByDefault() throws Exception {

        assertThat(baseUriProvider.getBaseUri(), is("http://localhost:8080"));
    }

    @Test
    public void shouldGetTheBaseUriWithSystemPropertyAsHostnameIfSet() throws Exception {

        System.setProperty(INTEGRATION_HOST_KEY, "my.funky.domain.com");

        assertThat(baseUriProvider.getBaseUri(), is("http://my.funky.domain.com:8080"));
    }

}