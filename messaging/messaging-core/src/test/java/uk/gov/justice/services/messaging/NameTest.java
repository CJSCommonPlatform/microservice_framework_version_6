package uk.gov.justice.services.messaging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.messaging.exception.InvalidMediaTypeException;

import org.junit.Test;

public class NameTest {

    private static final String MEDIA_TYPE = "application/vnd.cakeshop.add-recipe+json";
    private static final String NAME = "cakeshop.add-recipe";

    @Test
    public void shouldReturnValidName() {
        assertThat(Name.fromMediaType(MEDIA_TYPE).toString(), equalTo(NAME));
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void shouldThrowExceptionOnInvalidPrefix() {
        assertThat(Name.fromMediaType("application/invalid+json").toString(), equalTo(NAME));
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void shouldThrowExceptionOnInvalidSuffix() {
        assertThat(Name.fromMediaType("application/vnd.cakeshop.add-recipe.json").toString(), equalTo(NAME));
    }

}