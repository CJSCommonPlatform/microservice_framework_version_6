package uk.gov.justice.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.api.resource.DefaultUsersResource;
import uk.gov.justice.api.resource.DefaultUsersUserIdResource;

import org.junit.Test;

/**
 * Unit tests for the generated {@link QueryApiRestExampleApplication} class.
 */
public class QueryApiRestExampleApplicationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnCorrectClass() {
        QueryApiRestExampleApplication application = new QueryApiRestExampleApplication();
        assertThat(application.getClasses(), containsInAnyOrder(DefaultUsersUserIdResource.class, DefaultUsersResource.class));
    }
}
