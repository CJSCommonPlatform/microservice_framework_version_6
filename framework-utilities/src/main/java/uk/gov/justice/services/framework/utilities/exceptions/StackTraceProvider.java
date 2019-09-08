package uk.gov.justice.services.framework.utilities.exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class StackTraceProvider {

    public String getStackTrace(final Throwable throwable) {
        return ExceptionUtils.getStackTrace(throwable);
    }
}
