package uk.gov.justice.services.test.utils.core.random;

public class ValueGenerator<T> extends Generator<T> {

    private Iterable<T> values;

    public ValueGenerator(Iterable<T> values) {
        this.values = values;
    }

    @Override
    public T next() {
        return values.iterator().next();
    }
}
