package uk.gov.justice.services.event.sourcing.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorFileValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


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
            Assert.fail();
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
            Assert.fail();
        } catch (final SubscriptionLoadingException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is("Failed to load subscription yaml 'path/to/a/subscription.yaml' from classpath"));
        }
    }
}
