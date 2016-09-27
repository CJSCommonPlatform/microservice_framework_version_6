package uk.gov.justice.services.test.utils.core.random;

public class ValueGenerator<T> implements Generator<T> {

    private Iterable<T> values;

    public ValueGenerator(Iterable<T> values) {
        this.values = values;
    }

    public T next() {
        return values.iterator().next();
    }
}
