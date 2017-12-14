package uk.gov.justice.services.eventsourcing.source.core;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemEventServiceTest {

    private Clock clock = new UtcClock();

    @InjectMocks
    private SystemEventService systemEventService;

    @Before
    public void setup() {
        systemEventService.clock = clock;
    }

    @Test
    public void shouldCreateClonedEvent() {
        final UUID streamId = randomUUID();
        final JsonEnvelope event = systemEventService.clonedEventFor(streamId);

        assertThat(event, jsonEnvelope(
                metadata()
                        .withName("system.events.cloned")
                        .withUserId("system"),
                payloadIsJson(allOf(
                        withJsonPath("$.originatingStream", is(streamId.toString())),
                        withJsonPath("$.operation", is("cloned"))
                ))));
    }
}