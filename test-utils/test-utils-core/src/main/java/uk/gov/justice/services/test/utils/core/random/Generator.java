package uk.gov.justice.services.test.utils.core.random;

public interface Generator<T> {
    java.util.Random RANDOM = new java.util.Random();

    T next();
}
