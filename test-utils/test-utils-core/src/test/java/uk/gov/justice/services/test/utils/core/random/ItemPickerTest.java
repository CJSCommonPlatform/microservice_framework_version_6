package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class ItemPickerTest {

    private static final int NUMBER_OF_TIMES = 100000;

    @Test
    public void shouldRandomlyPickFromAvailableItems() {
        final List<Integer> items = newArrayList(1, 2, 3, 4, 5);

        final ItemPicker<Integer> generator = new ItemPicker<>(items);

        typeCheck(generator, items::contains).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldRandomlyPickFromAvailableItemsInArray() {
        final Integer[] items = {1, 2, 3, 4, 5};

        final ItemPicker<Integer> generator = new ItemPicker<>(items);

        typeCheck(generator, s -> newArrayList(items).contains(s)).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldPickAllElementsWhenRepeatedLargeNumberOfTimes() {
        final List<Integer> items = newArrayList(1, 2, 3, 4, 5);
        final Set<Integer> selectedItems = newHashSet();

        final ItemPicker<Integer> generator = new ItemPicker<>(items);

        typeCheck(generator, item -> {
            selectedItems.add(item);
            return items.contains(item);
        }).verify(times(NUMBER_OF_TIMES));

        assertThat(selectedItems.size(), is(equalTo(5)));
    }
}