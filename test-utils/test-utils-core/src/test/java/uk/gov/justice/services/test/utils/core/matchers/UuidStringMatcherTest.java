package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class UuidStringMatcherTest {

    @Test
    public void shouldMatchAValidUuid() throws Exception {
        assertThat(UUID.randomUUID().toString(), UuidStringMatcher.isAUuid());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchANonValidUuid() throws Exception {
        assertThat("79d0c503-052f-4105-9b05-b49d9c4cf6a", UuidStringMatcher.isAUuid());
    }
}