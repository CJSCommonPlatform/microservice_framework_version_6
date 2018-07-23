package uk.gov.justice.services.publishing.command.handler;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishingCommandHandlerTest {

    @Mock
    private EventSource eventSource;

    @InjectMocks
    private PublishingCommandHandler publishingCommandHandler;

    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> streamArgumentCaptor;

    @Before
    public void setup() throws Exception {
        createEnveloperWithEvents(RobotAdded.class);
    }

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        MatcherAssert.assertThat(publishingCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("addRobot").thatHandles("publish.command.add-robot")));
    }

    @Test
    public void shouldHandleTheRobotAddedCommand() throws Exception {

        final UUID robotId = randomUUID();
        final String robotType = "Protection Droid";
        final boolean evil = true;
        final boolean brainTheSizeOfAPlanet = true;

        final JsonEnvelope command = createCommand(robotId, robotType, evil, brainTheSizeOfAPlanet);

        final EventStream eventStream = mock(EventStream.class);

        when(eventSource.getStreamById(robotId)).thenReturn(eventStream);

        publishingCommandHandler.addRobot(command);

        verify(eventStream).append(streamArgumentCaptor.capture());

        final Stream<JsonEnvelope> envelopeStream = streamArgumentCaptor.getValue();

        final JsonEnvelope envelope = envelopeStream.findAny().get();
        final String payloadJson = envelope.payloadAsJsonObject().toString();


        assertThat(envelope.metadata().name(), is("publish.event.robot-added"));
        with(payloadJson)
                .assertThat("robotId", is(robotId.toString()))
                .assertThat("robotType", is(robotType))
                .assertThat("evil", is(evil))
                .assertThat("brainTheSizeOfAPlanet", is(brainTheSizeOfAPlanet))
        ;
    }

    @SuppressWarnings("SameParameterValue")
    private JsonEnvelope createCommand(
            final UUID robotId,
            final String robotType,
            final boolean evil,
            final boolean brainTheSizeOfAPlanet) {
        final UUID correlationId = randomUUID();
        final UUID sessionId = randomUUID();
        final UUID userId = randomUUID();
        final UUID id = randomUUID();
        final String commandName = "command.robot-added";

        return envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName(commandName)
                        .withClientCorrelationId(correlationId.toString())
                        .withSessionId(sessionId.toString())
                        .withUserId(userId.toString())
                        .withStreamId(robotId),
                createObjectBuilder()
                        .add("robotId", robotId.toString())
                        .add("robotType", robotType)
                        .add("isEvil", evil)
                        .add("brainTheSizeOfAPlanet", brainTheSizeOfAPlanet)
        );
    }
}
