package uk.gov.justice.services.core.json;

import static org.jboss.vfs.VFS.getChild;
import static org.jboss.vfs.VFSUtils.getPhysicalURL;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * @deprecated No longer required by Schema file validation
 */
@Deprecated
@ApplicationScoped
@Alternative
@Priority(200)
public class VfsUrlResolverStrategy implements FileSystemUrlResolverStrategy {
    @Override
    public URL getPhysicalFrom(final URL url) throws URISyntaxException, IOException {
        return getPhysicalURL(getChild(url.toURI()));
    }
}
