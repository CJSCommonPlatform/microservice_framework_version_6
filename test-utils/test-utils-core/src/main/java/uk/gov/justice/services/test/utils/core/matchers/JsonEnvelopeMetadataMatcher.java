package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.messaging.Metadata;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches the Metadata part of a JsonEnvelope. See {@link JsonEnvelopeMatcher} for usage example.
 */
public class JsonEnvelopeMetadataMatcher extends TypeSafeDiagnosingMatcher<Metadata> {

    private Optional<UUID> id = Optional.empty();
    private Optional<String> name = Optional.empty();
    private UUID[] causationIds = new UUID[]{};
    private Optional<String> userId = Optional.empty();
    private Optional<String> sessionId = Optional.empty();
    private Optional<UUID> streamId = Optional.empty();
    private Optional<Long> version = Optional.empty();

    public static JsonEnvelopeMetadataMatcher metadata() {
        return new JsonEnvelopeMetadataMatcher();
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Metadata with ");
        id.ifPresent(value -> description.appendText(format("id = %s, ", value)));
        name.ifPresent(value -> description.appendText(format("name = %s, ", value)));
        description.appendText(format("causationIds = %s, ", Arrays.toString(causationIds)));
        userId.ifPresent(value -> description.appendText(format("userId = %s, ", value)));
        sessionId.ifPresent(value -> description.appendText(format("sessionId = %s, ", value)));
        streamId.ifPresent(value -> description.appendText(format("streamId = %s, ", value)));
        version.ifPresent(value -> description.appendText(format("version = %s ", value)));
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
        this.causationIds = causationIds;
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
            description.appendText("Metadata with userId = " + metadata.userId());
            return false;
        }

        if (sessionIdIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with sessionId = " + metadata.sessionId());
            return false;
        }

        if (streamIdIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with streamId = " + metadata.streamId());
            return false;
        }

        if (versionIsSetAndDoesNotMatchWith(metadata)) {
            description.appendText("Metadata with version = " + metadata.version());
            return false;
        }

        return true;
    }

    private boolean idIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return !id.map(value -> value.equals(metadata.id())).orElse(true);
    }

    private boolean nameIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return !name.map(value -> value.equals(metadata.name())).orElse(true);
    }

    private boolean causationIdsDoNotMatchWith(final Metadata metadata) {
        return !containsInAnyOrder(causationIds).matches(metadata.causation());
    }

    private boolean userIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return !userId.equals(metadata.userId());
    }

    private boolean sessionIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return !sessionId.equals(metadata.sessionId());
    }

    private boolean streamIdIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return !streamId.equals(metadata.streamId());
    }

    private boolean versionIsSetAndDoesNotMatchWith(final Metadata metadata) {
        return !version.equals(metadata.version());
    }
}
