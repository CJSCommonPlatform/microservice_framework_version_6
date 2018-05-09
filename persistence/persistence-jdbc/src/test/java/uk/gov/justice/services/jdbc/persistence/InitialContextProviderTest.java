package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

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

import javax.naming.InitialContext;

@RunWith(MockitoJUnitRunner.class)
public class InitialContextProviderTest {

    @InjectMocks
    private InitialContextProvider initialContextProvider;

    @Test
    public void shouldCreateANewInitialContext() throws Exception {

        assertThat(initialContextProvider.getInitialContext(), is(instanceOf(InitialContext.class)));
    }

    @Test
    public void shouldCacheTheCreatedInitialContext() throws Exception {

        assertThat(initialContextProvider.getInitialContext(), is(sameInstance(initialContextProvider.getInitialContext())));
    }
}
