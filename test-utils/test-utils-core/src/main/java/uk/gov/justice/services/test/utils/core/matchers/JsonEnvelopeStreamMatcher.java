package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.stream.Stream;

import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches a Stream of JsonEnvelopes
 *
 * Example usage:
 * <pre>
 *  {@code
 *      assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
 *              jsonEnvelope(
 *                      metadata()
 *                          .withCausationIds(commandId)
 *                          .withName(EVENT_NAME),
 *                      payLoad().isJson(allOf(
 *                          withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
 *                          withJsonPath("$.name", equalTo(RECIPE_NAME)),
 *                          withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
 *                          withJsonPath("$.ingredients", empty())
 *                      ))))
 *      );
 * }
 * </pre>
 *
 * This makes use of {@link IsJson} to achieve Json matching in the payload.
 */
public class JsonEnvelopeStreamMatcher extends TypeSafeDiagnosingMatcher<Stream<JsonEnvelope>> {

    private final Matcher[] matchers;
    private List<JsonEnvelope> jsonEnvelopes;

    public JsonEnvelopeStreamMatcher(final Matcher... matchers) {
        this.matchers = matchers;
    }

    public static JsonEnvelopeStreamMatcher streamContaining(final Matcher... matchers) {
        return new JsonEnvelopeStreamMatcher(matchers);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope Stream that contains (");
        stream(matchers).forEach(description::appendDescriptionOf);
        description.appendText(")");
    }

    @Override
    protected boolean matchesSafely(final Stream<JsonEnvelope> jsonEnvelopeStream, final Description description) {
        if (jsonEnvelopes == null) {
            jsonEnvelopes = jsonEnvelopeStream.collect(toList());
        }

        return JsonEnvelopeListMatcher.listContaining(matchers).matchesSafely(jsonEnvelopes, description);
    }
}