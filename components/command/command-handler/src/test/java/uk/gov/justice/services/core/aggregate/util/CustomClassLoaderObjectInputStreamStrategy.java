package uk.gov.justice.services.core.aggregate.util;

import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomClassLoaderObjectInputStreamStrategy implements ObjectInputStreamStrategy {

    private final ClassLoader classLoader;

    public CustomClassLoaderObjectInputStreamStrategy(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ObjectInputStream objectInputStreamOf(final ByteArrayInputStream bis) throws IOException {
        return new CustomObjectInputStream(bis, classLoader);
    }
}
