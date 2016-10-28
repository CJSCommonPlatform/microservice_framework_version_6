package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.EmptyStreamMatcher.isEmptyStream;

import java.util.stream.Stream;

import org.junit.Test;

public class EmptyStreamMatcherTest {

    @Test
    public void shouldMatchAnEmptyStream() throws Exception {
        assertThat(Stream.empty(), isEmptyStream());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchANonEmptyStream() throws Exception {
        assertThat(Stream.of("test1", "test2"), isEmptyStream());
    }

}