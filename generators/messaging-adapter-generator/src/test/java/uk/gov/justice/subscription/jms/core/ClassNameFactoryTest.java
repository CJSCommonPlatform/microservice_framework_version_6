package uk.gov.justice.subscription.jms.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.squareup.javapoet.ClassName;
import org.junit.Test;

public class ClassNameFactoryTest {

    @Test
    public void shouldCreateClassNameFromBaseUriResourceUriAndClassNameSuffix() {
        final String basePackageName = "base.package";
        final String contextName = "my-context";
        final String componentName = "EVENT_LISTENER";
        final String jmsUri = "jms:topic:my-context.handler.command";

        final ClassNameFactory classNameFactory = new ClassNameFactory(
                basePackageName,
                contextName,
                componentName,
                jmsUri);

        final ClassName className = classNameFactory.classNameFor("ClassNameSuffix");

        assertThat(className.packageName(), is(basePackageName));
        assertThat(className.simpleName(), is("MyContextEventListenerMyContextHandlerCommandClassNameSuffix"));
    }
}
