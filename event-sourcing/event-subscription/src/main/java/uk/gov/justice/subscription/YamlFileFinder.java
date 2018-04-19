package uk.gov.justice.subscription;

import static java.lang.String.format;
import static java.nio.file.Paths.get;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;

public class YamlFileFinder {

    private static final String SUBSCRIPTION_FILE_NAME = "subscription-descriptor.yaml";
    private static final String EVENT_SOURCE_FILE_NAME = "event-sources.yaml";

    public List<Path> getSubscriptionDescriptorPaths(){
        return findOnClasspath(SUBSCRIPTION_FILE_NAME);
    }

    public List<Path> getEventSourcesPaths(){
        return findOnClasspath(EVENT_SOURCE_FILE_NAME);
    }


    private List<Path> findOnClasspath(final String name)  {
        try {
            final Enumeration<URL> resources = getClass()
                    .getClassLoader()
                    .getResources(name);

            return list(resources)
                    .stream()
                    .map(url -> get(url.getPath()))
                    .collect(toList());
        } catch (final IOException e) {
            throw new YamlFileLoadingException(format("Failed to load resources named '%s' from classpath", SUBSCRIPTION_FILE_NAME), e);
        }
    }
}
