package uk.gov.justice.services.messaging.subscription.cms;

import static java.nio.file.Paths.get;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ClasspathPathToAbsolutePathConverterTest {


    @InjectMocks
    private ClasspathPathToAbsolutePathConverter classpathPathToAbsolutePathConverter;

    @Test
    public void shouldConvertAClasspathUriToAnAbsolutePath() throws Exception {

        final Path subscriptionDefPath = get("subscriptions/my-context-event-listener-subscription-def.yaml");

        final Path absolutePath = classpathPathToAbsolutePathConverter.toAbsolutePath(subscriptionDefPath);

        final File file = absolutePath.toFile();

        assertThat(file.isAbsolute(), is(true));
        assertThat(file.exists(), is(true));
        assertThat(file.toString(), endsWith("/subscriptions/my-context-event-listener-subscription-def.yaml"));
    }
}
