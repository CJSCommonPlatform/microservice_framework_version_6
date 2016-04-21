package uk.gov.justice.services.clients.core;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertThat;

public class RestClientHelperTest {

    @Test
    public void shouldExtractPathParametersFromPathWithOneParam() {
        Set<String> pathParams = new RestClientHelper().extractPathParametersFromPath("/users/{userId}");
        assertThat(pathParams, IsCollectionWithSize.hasSize(1));
        assertThat(pathParams, IsCollectionContaining.hasItem("userId"));

    }

    @Test
    public void shouldExtractPathParametersFromPathWithTwoParams() {
        Set<String> pathParams = new RestClientHelper().extractPathParametersFromPath("/users/{lastName}/{dob}");
        assertThat(pathParams, IsCollectionWithSize.hasSize(2));
        assertThat(pathParams, IsCollectionContaining.hasItems("lastName", "dob"));
    }

    @Test
    public void shouldReturnEmptySetWhenNoParams() {
        Set<String> pathParams = new RestClientHelper().extractPathParametersFromPath("/users/");
        assertThat(pathParams, IsCollectionWithSize.hasSize(0));
    }

}