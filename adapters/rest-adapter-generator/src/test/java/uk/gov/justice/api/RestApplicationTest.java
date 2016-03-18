package uk.gov.justice.api;

import org.junit.Test;
import uk.gov.justice.api.resource.DefaultUsersUserIdResource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Unit tests for the generated {@link RestApplication} class.
 */
public class RestApplicationTest {

    @Test
    public void shouldReturnCorrectClass() {
        RestApplication application = new RestApplication();
        assertThat(application.getClasses(), containsInAnyOrder(DefaultUsersUserIdResource.class));
    }
}
