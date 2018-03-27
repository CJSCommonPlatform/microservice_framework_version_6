package uk.gov.justice.services.messaging.subscription.cms;

import static java.lang.String.format;

import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.maven.generator.io.files.parser.SubscriptionDescriptorFileValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.inject.Inject;

public class SubscriptionSpecProvider {

    @Inject
    ClasspathPathToAbsolutePathConverter classpathPathToAbsolutePathConverter;

    @Inject
    SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator;

    @Inject
    SubscriptionDescriptorDefLoader subscriptionDescriptorDefLoader;

    public SubscriptionDescriptorDef loadFromClasspath(final Path pathOnClasspath) {

        try {
            final Path absolutePath = classpathPathToAbsolutePathConverter.toAbsolutePath(
                    pathOnClasspath
            );

            subscriptionDescriptorFileValidator.validate(absolutePath);

            return subscriptionDescriptorDefLoader.loadFrom(absolutePath);

        } catch (final IOException | URISyntaxException e) {
            throw new SubscriptionLoadingException(format("Failed to load subscription yaml '%s' from classpath", pathOnClasspath), e);
        } 
    }





}
