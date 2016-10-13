package uk.gov.justice.domain.snapshot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DefaultObjectInputStreamStrategy implements ObjectInputStreamStrategy {

    public ObjectInputStream objectInputStreamOf(final ByteArrayInputStream bis) throws IOException {
        return new ObjectInputStream(bis);
    }
}
