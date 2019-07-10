package uk.gov.justice.services.framework.utilities.cdi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class CdiProviderIT {

    @Module
    @Classes(cdi = true, value = CdiProvider.class)
    public WebApp war() {
        return new WebApp()
                .contextRoot("CdiProviderIT")
                .addServlet("TestApp", Application.class.getName());
    }

    @Inject
    private CdiProvider cdiProvider;

    @Test
    public void shouldGetTheCurrentCdi() throws Exception {
        assertThat(cdiProvider.getCdi(), is(notNullValue()));
    }
}
