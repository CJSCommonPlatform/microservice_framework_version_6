package uk.gov.justice.services.test.utils.domain.arg;

public class ComplexArgument {

    private String stringArg;
    private Integer intArg;
    private Boolean booleanArg;

    public ComplexArgument(final String stringArg, final Integer intArg, final Boolean booleanArg) {
        this.stringArg = stringArg;
        this.intArg = intArg;
        this.booleanArg = booleanArg;
    }

    public String getStringArg() {
        return stringArg;
    }

    public Integer getIntArg() {
        return intArg;
    }

    public Boolean getBooleanArg() {
        return booleanArg;
    }
}
