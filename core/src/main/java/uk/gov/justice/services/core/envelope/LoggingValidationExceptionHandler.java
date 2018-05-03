package uk.gov.justice.services.core.envelope;

import static java.lang.String.format;

import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;

import javax.enterprise.inject.Alternative;

import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alternative
public class LoggingValidationExceptionHandler implements EnvelopeValidationExceptionHandler {

    private DefaultJsonValidationLoggerHelper defaultJsonValidationLoggerHelper = new DefaultJsonValidationLoggerHelper();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handle(final EnvelopeValidationException ex) {
        if(ex.getCause() instanceof ValidationException && logger.isWarnEnabled()) {
            logger.warn(traceValidation(ex), ex);
        } else if(logger.isWarnEnabled()) {
            logger.warn("Message validation failed ", ex);
        }
    }

    private String traceValidation(EnvelopeValidationException ex) {
        return format("Message validation failed %s",
                defaultJsonValidationLoggerHelper.toValidationTrace((ValidationException) ex.getCause()));
    }
}
