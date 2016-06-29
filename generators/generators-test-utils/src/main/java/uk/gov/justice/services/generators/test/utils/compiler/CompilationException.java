package uk.gov.justice.services.generators.test.utils.compiler;

public class CompilationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompilationException(final String string) {
        super(string);
    }


    public CompilationException(final Throwable throwable) {
        super(throwable);
    }
}
