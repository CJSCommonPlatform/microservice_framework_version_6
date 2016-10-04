package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class JsonEnvelopeMetadataMatcher extends TypeSafeMatcher<Metadata> {

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
        description.appendText(format("Metadata that has ( name = %s ) ", name));
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
    protected boolean matchesSafely(final Metadata metadata) {
        return id.map(value -> value.equals(metadata.id())).orElse(true)
                && name.map(value -> value.equals(metadata.name())).orElse(true)
                && containsInAnyOrder(causationIds).matches(metadata.causation())
                && userId.equals(metadata.userId())
                && sessionId.equals(metadata.sessionId())
                && streamId.equals(metadata.streamId())
                && version.equals(metadata.version());
    }
}
