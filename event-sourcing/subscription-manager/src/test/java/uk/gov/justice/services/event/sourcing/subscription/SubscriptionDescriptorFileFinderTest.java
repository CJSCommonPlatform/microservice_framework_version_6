package uk.gov.justice.services.event.sourcing.subscription;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorFileFinderTest {

    @InjectMocks
    private SubscriptionDescriptorFileFinder subscriptionDescriptorFileFinder;

    @Test
    public void shouldFindAllSubscriptionDescriptorsOnTheClasspathWhichHaveTheCorrectName() throws Exception {

        final List<Path> paths = subscriptionDescriptorFileFinder.findOnClasspath();

        assertThat(paths.size(), is(1));

        assertThat(paths.get(0).toString(), endsWith("/subscription-descriptor.yaml"));
    }
}
