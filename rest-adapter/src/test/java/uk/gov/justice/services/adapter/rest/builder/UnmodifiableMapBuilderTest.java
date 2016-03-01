package uk.gov.justice.services.adapter.rest.builder;

import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

public class UnmodifiableMapBuilderTest {

    @Test
    public void shouldBuildEmptyMap() {
        Map<String, String> map = new UnmodifiableMapBuilder<String, String>().build();
        assertThat(map.entrySet(), iterableWithSize(0));
    }

    @Test
    public void shouldBuildMapWithOneEntry() {
        Map<String, String> map = new UnmodifiableMapBuilder<String, String>().with("key123", "valueABC").build();
        assertThat(map.entrySet(), iterableWithSize(1));
        assertThat(map, hasEntry("key123", "valueABC"));
    }

    @Test
    public void shouldBuildMapWithThreeEntry() {
        Map<String, String> map = new UnmodifiableMapBuilder<String, String>().with("keyA", "v1").with("keyB", "v2")
                .with("keyC", "v3").build();
        assertThat(map.entrySet(), iterableWithSize(3));
        assertThat(map, hasEntry("keyA", "v1"));
        assertThat(map, hasEntry("keyB", "v2"));
        assertThat(map, hasEntry("keyC", "v3"));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void shouldThrowExceptionOnModificationAttempt() {
        Map<String, String> map = new UnmodifiableMapBuilder<String, String>().build();
        map.put("k1", "v1");
    }

}
