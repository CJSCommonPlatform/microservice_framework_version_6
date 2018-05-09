package uk.gov.justice.services.messaging.cdi;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class UnmanagedBeanCreatorTest {

    @Inject
    private UnmanagedBeanCreator unmanagedBeanCreator;

    @Module
    @Classes(cdi = true, value = {
            UnmanagedBeanCreator.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("custom-component-test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldCreateUnmanagedBean() {
        final TestBean testBean = unmanagedBeanCreator.create(TestBean.class);

        assertThat(testBean, is(CoreMatchers.notNullValue()));
        assertThat(testBean.beanManager, is(notNullValue()));
    }

    public static class TestBean {

        @Inject
        BeanManager beanManager;
    }
}