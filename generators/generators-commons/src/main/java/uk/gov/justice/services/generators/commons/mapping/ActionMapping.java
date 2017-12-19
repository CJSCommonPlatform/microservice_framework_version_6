package uk.gov.justice.services.generators.commons.mapping;

import static java.lang.String.format;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionType;
import static uk.gov.justice.services.generators.commons.helper.Actions.isSupportedActionTypeWithRequestType;

import org.raml.model.ActionType;

/**
 * Mapping between media types and framework actions
 */
public class ActionMapping {

    public static final String MAPPING_SEPARATOR = "(mapping):";
    public static final String REQUEST_TYPE_KEY = "requestType";
    public static final String RESPONSE_TYPE_KEY = "responseType";
    public static final String NAME_KEY = "name";
    public static final String MAPPING_BOUNDARY = "...";
    public static final String INVALID_ACTION_MAPPING_ERROR_MSG = "Invalid action mapping in RAML file";

    private final String requestType;
    private final String responseType;
    private final String name;

    public ActionMapping(final String name, final String requestType, final String responseType) {
        this.name = name;
        this.requestType = requestType;
        this.responseType = responseType;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getName() {
        return name;
    }

    public String getResponseType() {
        return responseType;
    }

    public String mimeTypeFor(final ActionType httpMethod) {
        if (isSupportedActionType(httpMethod)) {
            if (isSupportedActionTypeWithRequestType(httpMethod)) {
                return getRequestType();
            } else {
                return getResponseType();
            }
        }

        throw new IllegalArgumentException(format("Action %s not supported", httpMethod.toString()));
    }
}
