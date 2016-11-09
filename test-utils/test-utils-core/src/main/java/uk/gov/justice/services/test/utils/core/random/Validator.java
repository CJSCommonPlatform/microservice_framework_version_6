package uk.gov.justice.services.test.utils.core.random;

public interface Validator<T> {

    boolean validate(T objectToValidate);
}
