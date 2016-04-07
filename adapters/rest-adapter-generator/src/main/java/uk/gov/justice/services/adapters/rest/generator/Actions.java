package uk.gov.justice.services.adapters.rest.generator;


import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Response;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyList;

public class Actions {
    private Actions() {
    }

    public static Collection<MimeType> responseMimeTypesOf(final Action action) {
        Map<String, Response> responses = action.getResponses();
        return notEmpty(responses)
                && notEmpty(responses.values().iterator().next().getBody())
                ? responses.values().iterator().next().getBody().values()
                : emptyList();
    }

    private static boolean notEmpty(final Map<String, ?> responses) {
        return responses != null && responses.values().iterator().hasNext() && responses.values().iterator().next() != null;
    }
}
