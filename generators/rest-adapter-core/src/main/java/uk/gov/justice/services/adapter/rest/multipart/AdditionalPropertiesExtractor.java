package uk.gov.justice.services.adapter.rest.multipart;

import uk.gov.justice.services.adapter.rest.parameter.ParameterCollectionBuilder;
import uk.gov.justice.services.adapter.rest.parameter.ParameterType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;

public class AdditionalPropertiesExtractor {

    private AdditionalPropertiesExtractor() {
    }

    public static void addAdditionalProperties(final MultipartFormDataInput multipartFormDataInput, final ParameterCollectionBuilder validParameterCollectionBuilder, Logger logger) {
        for (Map.Entry<String, List<InputPart>> entry : multipartFormDataInput.getFormDataMap().entrySet()) {
            final String key = entry.getKey();

            final InputPart inputPart = entry.getValue().get(0);
            if(inputPart.getMediaType().getType().equals("text")) {
                try {
                    final String value = inputPart.getBodyAsString();
                    validParameterCollectionBuilder.putRequired(key, value, ParameterType.STRING);
                } catch (IOException e) {
                    logger.info(String.format("Key %s from multipart form has no value", e));
                }
            }
        }
    }
}
