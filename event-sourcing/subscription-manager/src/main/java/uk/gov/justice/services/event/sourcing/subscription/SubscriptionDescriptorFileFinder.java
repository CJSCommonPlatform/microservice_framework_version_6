package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;
import static java.nio.file.Paths.get;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;

public class SubscriptionDescriptorFileFinder {

    private static final String SUBSCRIPTION_FILE_NAME = "subscription-descriptor.yaml";

    public List<Path> findOnClasspath()  {

        try {
            final Enumeration<URL> resources = getClass()
                    .getClassLoader()
                    .getResources(SUBSCRIPTION_FILE_NAME);

            return list(resources)
                    .stream()
                    .map(url -> get(url.getPath()))
                    .collect(toList());
        } catch (final IOException e) {
            throw new SubscriptionLoadingException(format("Failed to load resources named '%s' from classpath", SUBSCRIPTION_FILE_NAME), e);
        }
    }
}
