package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches the Metadata part of a JsonEnvelope. See {@link JsonEnvelopeMatcher} for usage example.
 */
public class JsonEnvelopeMetadataMatcher extends TypeSafeDiagnosingMatcher<Metadata> {

    private static final String NOT_SET = "-- NOT SET --";

    private Optional<UUID> id = Optional.empty();
    private Optional<String> name = Optional.empty();
    private Optional<UUID[]> causationIds = Optional.empty();
    private Optional<String> userId = Optional.empty();
    private Optional<String> sessionId = Optional.empty();
    private Optional<UUID> streamId = Optional.empty();
    private Optional<Long> version = Optional.empty();
    private Optional<String> clientCorrelationId = Optional.empty();
    private Optional<IsJson<Object>> jsonMatcher = Optional.empty();

    /**
     * Create a metadata matcher instance.
     *
     * @return the matcher instance
     */
    public static JsonEnvelopeMetadataMatcher metadata() {
        return new JsonEnvelopeMetadataMatcher();
    }

    /**
     * Creates matcher that uses the metadata from the given JsonEnvelope as though the JsonEnvelope
     * under test was enveloped.
     *
     * @param jsonEnvelope the JsonEnvelope to extract the metadata from
     * @return the matcher instance
     */
    public static JsonEnvelopeMetadataMatcher withMetadataEnvelopedFrom(final Envelope<?> jsonEnvelope) {
        return metadata().envelopedWith(jsonEnvelope.metadata());
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Metadata with ");
        id.ifPresent(value -> description.appendText(format("id = %s, ", value)));
        name.ifPresent(value -> description.appendText(format("name = %s, ", value)));
        causationIds.ifPresent(value -> format("causationIds = %s", Arrays.toString(value)));
        userId.ifPresent(value -> description.appendText(format("userId = %s, ", value)));
        sessionId.ifPresent(value -> description.appendText(format("sessionId = %s, ", value)));
        streamId.ifPresent(value -> description.appendText(format("streamId = %s, ", value)));
        version.ifPresent(value -> description.appendText(format("version = %s ", value)));
        clientCorrelationId.ifPresent(value -> description.appendText(format("clientCorrelationId = %s ", value)));
        jsonMatcher.ifPresent(description::appendDescriptionOf);
    }

    public JsonEnvelopeMetadataMatcher withName(final String name) {
        this.name = Optional.of(name);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withId(final UUID id) {
        this.id = Optional.of(id);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withCausationIds(final UUID... causationIds) {
        this.causationIds = Optional.of(causationIds);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withUserId(final String userId) {
        this.userId = Optional.of(userId);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withSessionId(final String sessionId) {
        this.sessionId = Optional.of(sessionId);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withStreamId(final UUID streamId) {
        this.streamId = Optional.of(streamId);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withVersion(final Long version) {
        this.version = Optional.of(version);
        return this;
    }

    public JsonEnvelopeMetadataMatcher withClientCorrelationId(final String clientCorrelationId) {
        this.clientCorrelationId = Optional.of(clientCorrelationId);
        return this;
    }

    public JsonEnvelopeMetadataMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.jsonMatcher = Optional.of(new IsJson<>(matcher));
        return this;
    }

    /**
     * Directly match a given metadata instance
     *
     * @param metadata the metadata to match
     * @return the matcher instance
     */
    public JsonEnvelopeMetadataMatcher of(final Metadata metadata) {
        id = Optional.of(metadata.id());
        name = Optional.of(metadata.name());
        userId = metadata.userId();
        sessionId = metadata.sessionId();
        streamId = metadata.streamId();
        version = metadata.version();

        final List<UUID> causation = metadata.causation();
        causationIds = Optional.of(causation.toArray(new UUID[causation.size()]));

        return this;
    }

    /**
     * Does a match of a given metadata instance as though the JsonEnvelope was enveloped using the
     * given metadata. The id and name are ignored, and the id is added to the causation id list.
     *
     * @param metadata the metadata to match
     * @return the matcher instance
     */
    public JsonEnvelopeMetadataMatcher envelopedWith(final Metadata metadata) {
        id = Optional.empty();
        name = Optional.empty();
        userId = metadata.userId();
        sessionId = metadata.sessionId();
        streamId = metadata.streamId();
        version = metadata.version();

        final List<UUID> causation = metadata.causation();
        final UUID[] uuids = causation.toArray(new UUID[causation.size() + 1]);
        uuids[uuids.length - 1] = metadata.id();
        causationIds = Optional.of(uuids);

        return this;
    }

    @Override
    protected boolean matchesSafely(final Metadata metadata, final Description description) {
        if (idIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with id = " + metadata.id());
            return false;
        }

        if (nameIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with name = " + metadata.name());
            return false;
        }

        if (causationIdsDoNotMatchWith(metadata)) {
            description.appendText("Metadata with causationIds = " + metadata.causation());
            return false;
        }

        if (userIdIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with userId = " + metadata.userId().orElse(NOT_SET));
            return false;
        }

        if (sessionIdIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with sessionId = " + metadata.sessionId().orElse(NOT_SET));
            return false;
        }

        if (streamIdIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with streamId = " + metadata.streamId().map(UUID::toString).orElse(NOT_SET));
            return false;
        }

        if (versionIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with version = " + metadata.version().map(String::valueOf).orElse(NOT_SET));
            return false;
        }

        if (clientCorrelationIdIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with clientCorrelationId = " + metadata.clientCorrelationId().map(String::valueOf).orElse(NOT_SET));
            return false;
        }

        if (jsonMatcherIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata ");
            jsonMatcher.ifPresent(matcher -> matcher.describeMismatch(metadata.asJsonObject().toString(), description));
            return false;
        }

        return true;
    }

    private boolean idIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return id.isPresent() && !id.get().equals(metadata.id());
    }

    private boolean nameIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return name.isPresent() && !name.get().equals(metadata.name());
    }

    private boolean causationIdsDoNotMatchWith(final Metadata metadata) {
        return causationIds.isPresent() && !containsInAnyOrder(causationIds.get()).matches(metadata.causation());
    }

    private boolean userIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return userId.isPresent() && !userId.equals(metadata.userId());
    }

    private boolean sessionIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return sessionId.isPresent() && !sessionId.equals(metadata.sessionId());
    }

    private boolean streamIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return streamId.isPresent() && !streamId.equals(metadata.streamId());
    }

    private boolean versionIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return version.isPresent() && !version.equals(metadata.version());
    }

    private boolean clientCorrelationIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return clientCorrelationId.isPresent() && !clientCorrelationId.equals(metadata.clientCorrelationId());
    }

    private boolean jsonMatcherIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return jsonMatcher.isPresent() && !jsonMatcher.get().matches(metadata.asJsonObject().toString());
    }
}
