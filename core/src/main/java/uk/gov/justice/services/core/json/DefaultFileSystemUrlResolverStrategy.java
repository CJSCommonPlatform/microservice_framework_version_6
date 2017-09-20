package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(1)
public class DefaultFileSystemUrlResolverStrategy implements FileSystemUrlResolverStrategy {
    @Override
    public URL getPhysicalFrom(final URL url) throws URISyntaxException, IOException {
        return new URL(format("file:%s/", get(url.toURI()).getParent()));
    }
}
