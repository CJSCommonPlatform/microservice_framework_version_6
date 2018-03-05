package uk.gov.justice.raml.jms.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;

import org.junit.Test;

public class ClassNameFactoryTest {

    @Test
    public void shouldCreateClassNameFromBaseUriResourceUriAndClassNameSuffix() {
        final MessagingAdapterBaseUri baseUri = mock(MessagingAdapterBaseUri.class);
        final MessagingResourceUri resourceUri = mock(MessagingResourceUri.class);

        when(baseUri.toClassName()).thenReturn("BaseUri");
        when(resourceUri.toClassName()).thenReturn("ResourceUri");

        final String className = new ClassNameFactory(baseUri, resourceUri).classNameWith("ClassNameSuffix");

        assertThat(className, is("BaseUriResourceUriClassNameSuffix"));
    }
}