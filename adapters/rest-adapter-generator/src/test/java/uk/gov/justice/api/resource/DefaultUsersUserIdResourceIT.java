package uk.gov.justice.api.resource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.openejb.OpenEjbContainer;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultUsersUserIdResourceIT {
    private static final String JSON = "{\"userUrn\" : \"test\"}";

    private static EJBContainer container;
    private static Context context;

    private Client client;
    private WebTarget target;

    @BeforeClass
    public static void setUp() {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        container = EJBContainer.createEJBContainer(properties);
        context = container.getContext();
    }

    @AfterClass
    public static void close() throws NamingException, JMSException {
        context.close();
        container.close();
    }

    @Before
    public void before() throws NamingException, JMSException {
        context.bind("inject", this);
        client = new ResteasyClientBuilder().build();
        target = client.target("http://localhost:4204/rest-adapter-generator/users/1234");
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldCallCreateUser() throws Exception {
        Response response = target.request()
                .post(Entity.entity(JSON, "application/vnd.people.commands.create-user+json"));

        assertThat(response.getStatus(), is(202));
    }

    @Test
    public void shouldCallUpdateUser() throws Exception {
        Response response = target.request()
                .post(Entity.entity(JSON, "application/vnd.people.commands.update-user+json"));

        assertThat(response.getStatus(), is(202));
    }

}
