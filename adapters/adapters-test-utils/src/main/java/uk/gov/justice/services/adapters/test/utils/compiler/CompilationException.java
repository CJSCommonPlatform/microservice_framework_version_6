package uk.gov.justice.services.adapters.test.utils.compiler;

public class CompilationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompilationException(String string) {
        super(string);
    }


    public CompilationException(Throwable throwable) {
        super(throwable);
    }
}
