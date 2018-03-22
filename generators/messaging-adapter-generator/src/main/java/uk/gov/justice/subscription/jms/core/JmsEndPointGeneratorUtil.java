package uk.gov.justice.subscription.jms.core;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.domain.subscriptiondescriptor.Event;

import java.util.List;

public class JmsEndPointGeneratorUtil {
    /*
    Two things here:
    1. If raml contains general json (application/json) then it means that we accept all messages, so no filter should be generated
    2. For event listeners we let all message through the listener and then filter them after they pass event buffer service.
    Therefore we need a generated filter based on raml.

    Note: Event buffer service contains functionality that puts messages in correct order basing on version number,
    therefore we need all messages with consecutive numbers there. Messages need to be in correct order in order to update the view correctly.
    */

    private JmsEndPointGeneratorUtil() {

    }

    static boolean shouldGenerateEventFilter(final List<Event> events, final String component) {
        return component.contains(EVENT_LISTENER) && !events.isEmpty();
    }

    static boolean shouldListenToAllMessages(final List<Event> events, final String component) {
        return component.contains(EVENT_LISTENER) || events.isEmpty();
    }
}
