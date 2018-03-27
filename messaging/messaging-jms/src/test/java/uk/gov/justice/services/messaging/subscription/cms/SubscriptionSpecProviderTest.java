package uk.gov.justice.services.messaging.subscription.cms;

import static java.nio.file.Paths.get;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.maven.generator.io.files.parser.SubscriptionDescriptorFileValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionSpecProviderTest {

    @Mock
    private ClasspathPathToAbsolutePathConverter classpathPathToAbsolutePathConverter;

    @Mock
    private SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator;

    @Mock
    private SubscriptionDescriptorDefLoader subscriptionDescriptorDefLoader;

    @InjectMocks
    private SubscriptionSpecProvider subscriptionSpecProvider;

    @Test
    public void shouldLoadAndValidateASubscriptionObjectFromTheClasspath() throws Exception {

        final Path subscriptionDefPathOnClasspath = mock(Path.class);
        final Path absolutePath = mock(Path.class);
        final SubscriptionDescriptorDef subscriptionDescriptorDef = mock(SubscriptionDescriptorDef.class);

        when(classpathPathToAbsolutePathConverter.toAbsolutePath(subscriptionDefPathOnClasspath)).thenReturn(absolutePath);
        when(subscriptionDescriptorDefLoader.loadFrom(absolutePath)).thenReturn(subscriptionDescriptorDef);

        assertThat(subscriptionSpecProvider
                .loadFromClasspath(subscriptionDefPathOnClasspath), is(subscriptionDescriptorDef));

        final InOrder inOrder = inOrder(
                classpathPathToAbsolutePathConverter,
                subscriptionDescriptorFileValidator,
                subscriptionDescriptorDefLoader);

        inOrder.verify(classpathPathToAbsolutePathConverter).toAbsolutePath(subscriptionDefPathOnClasspath);
        inOrder.verify(subscriptionDescriptorFileValidator).validate(absolutePath);
        inOrder.verify(subscriptionDescriptorDefLoader).loadFrom(absolutePath);
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void shouldThrowASubscriptionLoadingExceptionIfGettingTheAbsolutePathFails() throws Exception {

        final Path subscriptionDefPathOnClasspath = mock(Path.class);

        final URISyntaxException uriSyntaxException = new URISyntaxException("this-file", "Ooops");

        when(classpathPathToAbsolutePathConverter.toAbsolutePath(subscriptionDefPathOnClasspath)).thenThrow(uriSyntaxException);
        when(subscriptionDefPathOnClasspath.toString()).thenReturn("path/to/a/subscription.yaml");

        try {
            subscriptionSpecProvider.loadFromClasspath(subscriptionDefPathOnClasspath);
            fail();
        } catch (final SubscriptionLoadingException expected) {
            assertThat(expected.getCause(), is(uriSyntaxException));
            assertThat(expected.getMessage(), is("Failed to load subscription yaml 'path/to/a/subscription.yaml' from classpath"));
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void shouldThrowASubscriptionLoadingExceptionIfLoadingTheYamlFileFaile() throws Exception {

        final IOException ioException = new IOException();

        final Path subscriptionDefPathOnClasspath = mock(Path.class);
        final Path absolutePath = mock(Path.class);

        when(classpathPathToAbsolutePathConverter.toAbsolutePath(subscriptionDefPathOnClasspath)).thenReturn(absolutePath);
        when(subscriptionDescriptorDefLoader.loadFrom(absolutePath)).thenThrow(ioException);
        when(subscriptionDefPathOnClasspath.toString()).thenReturn("path/to/a/subscription.yaml");

        try {
            subscriptionSpecProvider.loadFromClasspath(subscriptionDefPathOnClasspath);
            fail();
        } catch (final SubscriptionLoadingException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is("Failed to load subscription yaml 'path/to/a/subscription.yaml' from classpath"));
        }
    }
}
