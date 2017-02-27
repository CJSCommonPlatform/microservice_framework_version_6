package uk.gov.justice.services.generators.commons.validator;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import java.util.List;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.ParamType;
import org.raml.model.Resource;
import org.raml.model.parameter.FormParameter;

public class MultipartHasFormParameters extends AbstractResourceRamlValidator {

    @Override
    protected void validate(final Resource resource) {
        final Map<ActionType, Action> actions = resource.getActions();

        if (!actions.isEmpty()) {
            actions.values().forEach(this::extractBodyMimeTypes);
        }
    }

    private void extractBodyMimeTypes(final Action action) {
        final Map<String, MimeType> body = action.getBody();
        if (body != null) {
            body.values().stream()
                    .filter(mimeType -> MULTIPART_FORM_DATA.equals(mimeType.getType()))
                    .forEach(this::validateFormParameters);
        }
    }

    private void validateFormParameters(final MimeType mimeType) {
        final Map<String, List<FormParameter>> formParameters = mimeType.getFormParameters();

        if (null == formParameters || formParameters.isEmpty()) {
            throw new RamlValidationException("Multipart form must contain form parameters");
        }

        formParameters.values().forEach(this::validateFormParameter);
    }

    private void validateFormParameter(final List<FormParameter> values) {
        final FormParameter formParameter = values.get(0);

        if (!ParamType.FILE.equals(formParameter.getType())) {
            throw new RamlValidationException(format("Multipart form parameter is expected to be of type FILE, instead was %s", values.get(0).getType()));
        }
    }
}
