package uk.gov.justice.services.event.publisher.it;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.envelopes.EnvelopeStreamGenerator.envelopeStreamGenerator;
import static uk.gov.justice.services.test.utils.core.envelopes.StreamDefBuilder.aStream;

import uk.gov.justice.services.event.publisher.it.helpers.AddRobotEnvelopeGenerator;
import uk.gov.justice.services.event.publisher.it.helpers.JmsMessageReceiver;
import uk.gov.justice.services.event.publisher.it.helpers.JmsMessageSender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.envelopes.EnvelopeStreamGenerator;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.jms.JMSException;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventPublishingIT {

    private static final String ADD_ROBOT_COMMAND = "publish.command.add-robot";
    private static final String ROBOT_ADDED_EVENT = "publish.event.robot-added";

    private final JmsMessageSender jmsMessageSender = new JmsMessageSender();
    private final JmsMessageReceiver jmsMessageReceiver = new JmsMessageReceiver();
    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();
    private final AddRobotEnvelopeGenerator addRobotEnvelopeGenerator = new AddRobotEnvelopeGenerator();
    private final Poller poller = new Poller(600, 500);

    @Before
    public void cleanEventStoreDatabase() {
        databaseCleaner.cleanEventStoreTables("framework");
    }

    @Before
    public void startMessaging() {
        jmsMessageSender.startSession();
    }

    @After
    public void closeMessaging() throws JMSException {
        jmsMessageSender.close();
    }

    @Test
    public void shouldPublishOneEvent() throws Exception {

        final int numberOfMessages = 1;

        final UUID robotId = randomUUID();
        final JsonEnvelope jsonEnvelope = addRobotEnvelopeGenerator.generate(robotId, 1L);

        jmsMessageReceiver.startTopicListener(ROBOT_ADDED_EVENT, numberOfMessages);

        jmsMessageSender.sendCommandToQueue(jsonEnvelope, ADD_ROBOT_COMMAND);

        final Optional<List<String>> events = poller.pollUntilFound(() -> jmsMessageReceiver.getMessagesFromTopic(numberOfMessages));

        if (events.isPresent()) {

            final List<String> messages = events.get();
            assertThat(messages.size(), is(numberOfMessages));

            with(messages.get(0))
                    .assertThat("$._metadata.name", is(ROBOT_ADDED_EVENT))
                    .assertThat("$._metadata.stream.id", is(robotId.toString()))
                    .assertThat("$.robotId", is(robotId.toString()))
                    .assertThat("$.robotType", is("Protection Droid 1"))
                    .assertThat("$.evil", is(true))
                    .assertThat("$.brainTheSizeOfAPlanet", is(true))
            ;

        } else {
            fail("Failed to get message from topic");
        }
    }

    @Test
    public void shouldPublishManyEventsToDiversStreams() throws Exception {

        final int streamSize = 100;
        final int numberOfStreams = 4;

        final EnvelopeStreamGenerator envelopeStreamGenerator = envelopeStreamGenerator();

        for(int i = 0; i < numberOfStreams; i++) {
            envelopeStreamGenerator.withStreamOf(aStream()
                    .withStreamId(randomUUID())
                    .withStreamSize(streamSize)
                    .withEnvelopeCreator(() -> addRobotEnvelopeGenerator)
            );
        }

        final List<JsonEnvelope> jsonEnvelopes = envelopeStreamGenerator.generateAll();


        jmsMessageReceiver.startTopicListener(ROBOT_ADDED_EVENT, jsonEnvelopes.size());


        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        jsonEnvelopes.forEach(jsonEnvelope -> jmsMessageSender
                .sendCommandToQueue(jsonEnvelope, ADD_ROBOT_COMMAND));


        final Optional<List<String>> events = poller.pollUntilFound(() -> jmsMessageReceiver.getMessagesFromTopic(jsonEnvelopes.size()));

        stopWatch.stop();

        if(events.isPresent()) {

            System.out.println("Publishing " + jsonEnvelopes.size() + " commands took " + stopWatch.getTime() + " milliseconds");

            assertThat(events.get().size(), is(jsonEnvelopes.size()));
        } else {
            fail();
        }
    }
}
