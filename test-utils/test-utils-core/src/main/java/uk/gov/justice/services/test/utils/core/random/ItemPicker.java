package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public class ItemPicker<T> extends Generator<T> {

    private final List<T> items;

    public ItemPicker(final T... items) {
        this.items = newArrayList(items);
    }

    public ItemPicker(final List<T> items) {
        this.items = items;
    }

    @Override
    public T next() {
        return items.get(RANDOM.nextInt(items.size()));
    }
}
