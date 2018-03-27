package uk.gov.justice.services.messaging.subscription.cms;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClasspathPathToAbsolutePathConverter {

    public Path toAbsolutePath(final Path pathOnClasspath) throws URISyntaxException {
        final URL subscriptionDef = getClass()
                .getClassLoader()
                .getResource(pathOnClasspath.toString());

        return Paths.get(subscriptionDef.toURI());
    }
}
