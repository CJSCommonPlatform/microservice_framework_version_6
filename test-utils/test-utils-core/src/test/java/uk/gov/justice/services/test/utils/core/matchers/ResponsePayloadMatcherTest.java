package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.UUID;

import uk.gov.justice.services.test.utils.core.http.ResponseData;

import javax.json.JsonArrayBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResponsePayloadMatcherTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldMatchJsonPayloadFromResponse() throws Exception {
        final String userId1 = UUID.next().toString();
        final String userId2 = UUID.next().toString();

        final JsonArrayBuilder events = createArrayBuilder()
                .add(createObjectBuilder().add("userId", userId1).build())
                .add(createObjectBuilder().add("userId", userId2).build());

        final String eventsJson = createObjectBuilder().add("events", events).build().toString();

        assertThat(new ResponseData(null, eventsJson), payload()
                .isJson(allOf(
                        withJsonPath("$.events", hasSize(2)),
                        withJsonPath("$.events[0].userId", is(userId1)),
                        withJsonPath("$.events[1].userId", is(userId2))
                )));
    }

    @Test
    public void shouldFailWhenJsonResponsePayloadDoesNotMatch() throws Exception {
        final JsonArrayBuilder events = createArrayBuilder();
        final String emptyEventsJson = createObjectBuilder().add("events", events).build().toString();

        expectedException.expect(AssertionError.class);

        assertThat(new ResponseData(null, emptyEventsJson), payload()
                .isJson(
                        withJsonPath("$.events", hasSize(2))
                ));
    }

    @Test
    public void shouldMatchStringPayloadFromResponse() {
        assertThat(new ResponseData(null, "string payload data"), payload()
                .that(
                        containsString("string payload")
                ));
    }

    @Test
    public void shouldFailWhenStringResponsePayloadDoesNotMatch() {
        expectedException.expect(AssertionError.class);

        assertThat(new ResponseData(null, "string payload data"), payload()
                .that(
                        containsString("does not exist")
                ));
    }

    @Test
    public void shouldMatchBothStringAndJsonPayloadFromResponse() {
        final String userId1 = UUID.next().toString();

        final JsonArrayBuilder events = createArrayBuilder()
                .add(createObjectBuilder().add("userId", userId1).build());

        final String eventsJson = createObjectBuilder().add("events", events).build().toString();

        assertThat(new ResponseData(null, eventsJson), payload()
                .that(containsString(userId1))
                .isJson(allOf(
                        withJsonPath("$.events", hasSize(1)),
                        withJsonPath("$.events[0].userId", is(userId1))
                )));
    }
}