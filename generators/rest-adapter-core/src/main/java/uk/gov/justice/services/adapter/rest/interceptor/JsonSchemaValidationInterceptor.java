package uk.gov.justice.services.adapter.rest.interceptor;

import static java.nio.charset.Charset.defaultCharset;
import static javax.ws.rs.core.MediaType.CHARSET_PARAMETER;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.json.JsonValidationLogger.toValidationTrace;
import static uk.gov.justice.services.messaging.logging.HttpMessageLoggerHelper.toHttpHeaderTrace;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.messaging.Name;
import uk.gov.justice.services.messaging.exception.InvalidMediaTypeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;

/**
 * Intercepts incoming REST requests and if they are POSTs, check that the JSON payload is valid
 * against the relevant JSON schema.
 */
@Provider
public class JsonSchemaValidationInterceptor implements ReaderInterceptor {

    private static final Logger LOGGER = getLogger(JsonSchemaValidationInterceptor.class.getName());
    private static final String JSON_MEDIA_TYPE_FORMAT = "+json";
    @Inject
    JsonSchemaValidator validator;

    @Override
    public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {

        if (context.getMediaType().getSubtype().endsWith(JSON_MEDIA_TYPE_FORMAT)) {
            final String charset = extractCharset(context);
            final String payload = IOUtils.toString(context.getInputStream(), charset);
            try {
                validator.validate(payload, extractName(context));
            } catch (ValidationException | InvalidMediaTypeException ex) {

                if(ex instanceof ValidationException) {
                    final String message = String.format("JSON schema validation has failed on %s due to %s ",
                            toHttpHeaderTrace(context.getHeaders()),
                            toValidationTrace((ValidationException) ex));
                    LOGGER.debug(message);
                    throw new BadRequestException(message, ex);
                }
                throw new BadRequestException(ex.getMessage(), ex);
            }
            context.setInputStream(new ByteArrayInputStream(payload.getBytes(charset)));
        }
        return context.proceed();
    }

    private String extractName(final ReaderInterceptorContext context) {
        return Name.fromMediaType(context.getMediaType().toString()).toString();
    }

    private String extractCharset(final ReaderInterceptorContext context) {
        final Map<String, String> params = context.getMediaType().getParameters();
        return params.containsKey(CHARSET_PARAMETER) ? params.get(CHARSET_PARAMETER) : defaultCharset().name();
    }
}
