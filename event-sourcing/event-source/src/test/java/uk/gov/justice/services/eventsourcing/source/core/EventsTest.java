package uk.gov.justice.services.eventsourcing.source.core;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

public class EventsTest {

    private static final Object OBJECT_1 = new Object();
    private static final Object OBJECT_2 = new Object();

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Events.class);
    }

    @Test
    public void shouldReturnStreamOfObjects() {
        List<Object> objects = Events.streamOf(OBJECT_1, OBJECT_2).collect(Collectors.toList());
        assertThat(objects, IsIterableContainingInOrder.contains(OBJECT_1, OBJECT_2));
    }

}