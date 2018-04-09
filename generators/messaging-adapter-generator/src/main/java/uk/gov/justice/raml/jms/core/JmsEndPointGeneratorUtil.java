package uk.gov.justice.raml.jms.core;

import static uk.gov.justice.raml.jms.core.MediaTypesUtil.containsGeneralJsonMimeType;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;

import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;

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

    static boolean shouldGenerateEventFilter(final Resource resource, final MessagingAdapterBaseUri baseUri) {
        return EVENT_LISTENER.equals(baseUri.component()) && !containsGeneralJsonMimeType(resource.getActions());
    }

    static boolean shouldListenToAllMessages(final Map<ActionType, Action> resourceActions, final MessagingAdapterBaseUri baseUri) {
        return EVENT_LISTENER.equals(baseUri.component()) || containsGeneralJsonMimeType(resourceActions);
    }
}
