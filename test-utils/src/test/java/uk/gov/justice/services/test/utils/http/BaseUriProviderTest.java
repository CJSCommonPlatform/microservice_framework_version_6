package uk.gov.justice.services.test.utils.http;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.http.BaseUriProvider.INTEGRATION_HOST_KEY;

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
