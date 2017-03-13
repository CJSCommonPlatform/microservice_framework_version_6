package uk.gov.justice.services.generators.test.utils.builder;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.raml.model.ParamType.FILE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.MimeType;
import org.raml.model.ParamType;
import org.raml.model.parameter.FormParameter;

public class MimeTypeBuilder {

    final Map<String, List<FormParameter>> formParameters = new HashMap<>();
    private final String type;

    public MimeTypeBuilder(final String type) {
        this.type = type;
    }

    public static MimeTypeBuilder multipartMimeType() {
        return new MimeTypeBuilder(MULTIPART_FORM_DATA);
    }

    public static MimeTypeBuilder multipartWithFileFormParameter(final String fieldName) {
        final MimeTypeBuilder mimeTypeBuilder = multipartMimeType();
        mimeTypeBuilder.withRequiredFileTypeFormParameter(fieldName);
        return mimeTypeBuilder;
    }

    public MimeTypeBuilder withRequiredFileTypeFormParameter(final String fieldName) {
        return withFormParameter(fieldName, FILE, true);
    }

    public MimeTypeBuilder withRequiredFormParameter(final String fieldName, final ParamType paramType) {
        return withFormParameter(fieldName, paramType, true);
    }

    public MimeTypeBuilder withOptionalFormParameter(final String fieldName, final ParamType paramType) {
        return withFormParameter(fieldName, paramType, false);
    }

    public MimeTypeBuilder withNoDisplayNameFormParameter(final String fieldName, final ParamType paramType) {
        final FormParameter formParameter = new FormParameter();
        formParameter.setType(paramType);
        formParameter.setRequired(true);

        formParameters.put(fieldName, singletonList(formParameter));
        return this;
    }

    public MimeTypeBuilder withFormParameter(final String fieldName, final ParamType paramType, final boolean required) {
        final FormParameter formParameter = new FormParameter();
        formParameter.setType(paramType);
        formParameter.setRequired(required);

        formParameters.put(fieldName, singletonList(formParameter));
        return this;
    }

    public MimeType build() {
        final MimeType mimeType = new MimeType(type);
        mimeType.setFormParameters(formParameters);
        return mimeType;
    }
}
