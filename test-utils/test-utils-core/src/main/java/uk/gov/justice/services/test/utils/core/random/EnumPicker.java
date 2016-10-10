package uk.gov.justice.services.test.utils.core.random;

import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;

public class EnumPicker<T extends Enum<?>> extends Generator<T> {

    private final Class<T> clazz;
    private final Generator<Integer> generator;

    public EnumPicker(final Class<T> clazz) {
        this.clazz = clazz;
        this.generator = integer(clazz.getEnumConstants().length);
    }

    @Override
    public T next() {
        return this.clazz.getEnumConstants()[generator.next()];
    }
}
