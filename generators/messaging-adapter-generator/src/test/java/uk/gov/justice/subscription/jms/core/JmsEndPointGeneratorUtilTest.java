package uk.gov.justice.subscription.jms.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.domain.EventBuilder.event;
import static uk.gov.justice.subscription.jms.core.JmsEndPointGeneratorUtil.shouldGenerateEventFilter;
import static uk.gov.justice.subscription.jms.core.JmsEndPointGeneratorUtil.shouldListenToAllMessages;

import uk.gov.justice.domain.subscriptiondescriptor.Event;

import java.util.Arrays;

import org.junit.Test;

public class JmsEndPointGeneratorUtilTest {

    @Test
    public void shouldGenerateEventFilterCheck() {

        final Event event = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        boolean shouldGenerateEventFilter = shouldGenerateEventFilter(Arrays.asList(event), "EVENT_LISTENER");

        assertThat(shouldGenerateEventFilter, is(true));
    }

    @Test
    public void shouldListenToAllMessagesCheck() {

        final Event event = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        boolean shouldGenerateEventFilter = shouldListenToAllMessages(Arrays.asList(event), "EVENT_LISTENER");

        assertThat(shouldGenerateEventFilter, is(true));
    }
}
