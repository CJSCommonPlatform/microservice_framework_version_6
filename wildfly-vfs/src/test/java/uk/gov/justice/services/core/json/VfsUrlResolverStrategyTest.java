package uk.gov.justice.services.core.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

public class VfsUrlResolverStrategyTest {

    @Test
    public void shouldReturnPhysicalVfsUrl() throws IOException, URISyntaxException {
        final VfsUrlResolverStrategy vfsUrlResolverStrategy = new VfsUrlResolverStrategy();
        final String url = "file:/something/here";
        final URL physicalUrl = vfsUrlResolverStrategy.getPhysicalFrom(new URL(url));
        assertThat(physicalUrl.toString(), is(url));
    }
}