package uk.gov.justice.services.core.json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public interface FileSystemUrlResolverStrategy {

   URL getPhysicalFrom(final URL url) throws URISyntaxException, IOException;

}
