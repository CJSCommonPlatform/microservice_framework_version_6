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

public class EnvelopeMetadataMatcher extends TypeSafeDiagnosingMatcher<Metadata> {

    private static final String NOT_SET = "-- NOT SET --";

    private Optional<UUID> id = Optional.empty();
    private Optional<String> name = Optional.empty();
    private Optional<UUID[]> causationIds = Optional.empty();
    private Optional<String> userId = Optional.empty();
    private Optional<String> sessionId = Optional.empty();
    private Optional<UUID> streamId = Optional.empty();
    private Optional<Long> position = Optional.empty();
    private Optional<String> clientCorrelationId = Optional.empty();
    private Optional<IsJson<Object>> jsonMatcher = Optional.empty();

    /**
     * Create a metadata matcher instance.
     *
     * @return the matcher instance
     */
    public static EnvelopeMetadataMatcher metadata() {
        return new EnvelopeMetadataMatcher();
    }

    /**
     * Creates matcher that uses the metadata from the given JsonEnvelope as though the JsonEnvelope
     * under test was enveloped.
     *
     * @param jsonEnvelope the JsonEnvelope to extract the metadata from
     * @return the matcher instance
     */
    public static EnvelopeMetadataMatcher withMetadataEnvelopedFrom(final Envelope<?> jsonEnvelope) {
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
        position.ifPresent(value -> description.appendText(format("position = %s ", value)));
        clientCorrelationId.ifPresent(value -> description.appendText(format("clientCorrelationId = %s ", value)));
        jsonMatcher.ifPresent(description::appendDescriptionOf);
    }

    public EnvelopeMetadataMatcher withName(final String name) {
        this.name = Optional.of(name);
        return this;
    }

    public EnvelopeMetadataMatcher withId(final UUID id) {
        this.id = Optional.of(id);
        return this;
    }

    public EnvelopeMetadataMatcher withCausationIds(final UUID... causationIds) {
        this.causationIds = Optional.of(causationIds);
        return this;
    }

    public EnvelopeMetadataMatcher withUserId(final String userId) {
        this.userId = Optional.of(userId);
        return this;
    }

    public EnvelopeMetadataMatcher withSessionId(final String sessionId) {
        this.sessionId = Optional.of(sessionId);
        return this;
    }

    public EnvelopeMetadataMatcher withStreamId(final UUID streamId) {
        this.streamId = Optional.of(streamId);
        return this;
    }

    public EnvelopeMetadataMatcher withPosition(final Long position) {
        this.position = Optional.of(position);
        return this;
    }

    /**
     * deprecated.
     * version renamed to position. Please use <code>withPosition(...)</code> instead.
     *
     * @param version The position of this event
     * @return this
     */
    @Deprecated // renamed to position. Please use instead
    public EnvelopeMetadataMatcher withVersion(final Long version) {
        this.position = Optional.of(version);
        return this;
    }

    public EnvelopeMetadataMatcher withClientCorrelationId(final String clientCorrelationId) {
        this.clientCorrelationId = Optional.of(clientCorrelationId);
        return this;
    }

    public EnvelopeMetadataMatcher isJson(final Matcher<? super ReadContext> matcher) {
        this.jsonMatcher = Optional.of(new IsJson<>(matcher));
        return this;
    }

    /**
     * Directly match a given metadata instance
     *
     * @param metadata the metadata to match
     * @return the matcher instance
     */
    public EnvelopeMetadataMatcher of(final Metadata metadata) {
        id = Optional.of(metadata.id());
        name = Optional.of(metadata.name());
        userId = metadata.userId();
        sessionId = metadata.sessionId();
        streamId = metadata.streamId();
        position = metadata.position();

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
    public EnvelopeMetadataMatcher envelopedWith(final Metadata metadata) {
        id = Optional.empty();
        name = Optional.empty();
        userId = metadata.userId();
        sessionId = metadata.sessionId();
        streamId = metadata.streamId();
        position = metadata.position();

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

        if (positionIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with position = " + metadata.position().map(String::valueOf).orElse(NOT_SET));
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

    private boolean positionIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return position.isPresent() && !position.equals(metadata.position());
    }

    private boolean clientCorrelationIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return clientCorrelationId.isPresent() && !clientCorrelationId.equals(metadata.clientCorrelationId());
    }

    private boolean jsonMatcherIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return jsonMatcher.isPresent() && !jsonMatcher.get().matches(metadata.asJsonObject().toString());
    }

}
