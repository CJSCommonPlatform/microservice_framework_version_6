package uk.gov.justice.api.resource;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.justice.api.RestApplication;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.example.DummyDispatcher;

import javax.ws.rs.core.Response;
import java.util.Properties;

import static javax.ws.rs.client.Entity.entity;
import static org.apache.cxf.jaxrs.client.WebClient.create;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for the generated JAX-RS classes.
 */
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class DefaultUsersUserIdResourceIT {

    private static int port = -1;
    private static String BASE_URI = "undefined";

    private static final String BASE_URI_PATTERN = "http://localhost:%d/rest-adapter-generator/rest";

    private static final String JSON = "{\"userUrn\" : \"test\"}";

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        BASE_URI = String.format(BASE_URI_PATTERN, port);
    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            RestProcessor.class,
            RestEnvelopeBuilderFactory.class,
            DummyDispatcher.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-adapter-generator")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", RestApplication.class.getName());
    }

    @Test
    public void shouldCallCreateUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, "application/vnd.people.commands.create-user+json"));

        assertThat(response.getStatus(), is(202));
    }

    @Test
    public void shouldCallUpdateUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, "application/vnd.people.commands.update-user+json"));

        assertThat(response.getStatus(), is(202));
    }
}
