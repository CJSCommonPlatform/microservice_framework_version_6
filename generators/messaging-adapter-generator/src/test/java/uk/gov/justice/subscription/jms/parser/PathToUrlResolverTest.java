package uk.gov.justice.subscription.jms.parser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class PathToUrlResolverTest {

    @Test
    public void shouldResolvePathToUrl() {
        final Path baseDir = Paths.get("/yaml");
        final Path path = Paths.get("subscriptions-descriptor.yaml");

        final URL url = new PathToUrlResolver().resolveToUrl(baseDir, path);

        assertThat(url.toString(), is("file:/yaml/subscriptions-descriptor.yaml"));
    }
}