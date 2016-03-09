package uk.gov.justice.services.adapter.rest.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class that builds an immutable map
 *
 */
public class UnmodifiableMapBuilder<K, T> {
    private final Map<K, T> map = new HashMap<>();

    /**
     * @param key
     * @param value
     * @return - builder instance with key and value set
     */
    public UnmodifiableMapBuilder<K, T> with(final K key, final T value) {
        map.put(key, value);
        return this;
    }

    /**
     * @return an immutable map with entries set by invocations of the with
     *         method
     */
    public Map<K, T> build() {
        return Collections.unmodifiableMap(map);
    }
}
