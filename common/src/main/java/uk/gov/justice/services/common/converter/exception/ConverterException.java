package uk.gov.justice.services.common.converter.exception;

public class ConverterException extends RuntimeException {

    private static final long serialVersionUID = 4101915933800790698L;

    public ConverterException(final String message) {
        super(message);
    }

    public ConverterException(final String message, Throwable cause) {
        super(message, cause);
    }

}
