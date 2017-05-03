package uk.gov.justice.services.adapter.direct;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class SynchronousDirectAdapterCacheIT {

    @Inject
    private SynchronousDirectAdapterCache adapterCache;

    @Module
    @Classes(cdi = true, value = {
            SynchronousDirectAdapterCache.class,
            DirectAdapterAbc.class,
            DirectAdapterBcd.class

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldProduceAdapterBasingOnServiceComponentAnnotation() throws Exception {
        assertThat(adapterCache.directAdapterForComponent("COMPONENT_ABC"), instanceOf(DirectAdapterAbc.class));
        assertThat(adapterCache.directAdapterForComponent("COMPONENT_BCD"), instanceOf(DirectAdapterBcd.class));

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfBeanNotFound() throws Exception {
        adapterCache.directAdapterForComponent("UNKNOWN");
    }

    @DirectAdapter("COMPONENT_ABC")
    public static class DirectAdapterAbc implements SynchronousDirectAdapter {

        @Override
        public JsonEnvelope process(JsonEnvelope envelope) {
            return null;
        }
    }

    @DirectAdapter("COMPONENT_BCD")
    public static class DirectAdapterBcd implements SynchronousDirectAdapter {

        @Override
        public JsonEnvelope process(JsonEnvelope envelope) {
            return null;
        }
    }
}