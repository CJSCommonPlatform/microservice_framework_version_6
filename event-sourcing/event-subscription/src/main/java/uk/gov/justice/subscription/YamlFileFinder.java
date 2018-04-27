package uk.gov.justice.subscription;

import static java.lang.String.format;
import static java.util.Collections.list;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Find yaml/subscription-descriptor.yaml and yaml/event-sources.yaml resources on the classpath.
 */
public class YamlFileFinder {

    private static final String SUBSCRIPTION_FILE_NAME = "yaml/subscription-descriptor.yaml";
    private static final String EVENT_SOURCE_FILE_NAME = "yaml/event-sources.yaml";

    @Inject
    Logger logger;

    /**
     * Find all yaml/subscription-descriptor.yaml resources on the classpath
     *
     * @return List of subscription-descriptor.yaml URLs
     * @throws IOException if getting the resources fails
     */
    public List<URL> getSubscriptionDescriptorPaths() throws IOException {
        return findOnClasspath(SUBSCRIPTION_FILE_NAME);
    }

    /**
     * Find all yaml/event-sources.yaml resources on the classpath
     *
     * @return List of event-sources.yaml URLs
     * @throws IOException if getting the resources fails
     */
    public List<URL> getEventSourcesPaths() throws IOException {
        return findOnClasspath(EVENT_SOURCE_FILE_NAME);
    }

    private List<URL> findOnClasspath(final String name) throws IOException {
        final List<URL> urls = list(getClass()
                .getClassLoader()
                .getResources(name));

        logger.info(format("Found %s resources on the classpath for %s", urls.size(), name));

        return urls;
    }
}
