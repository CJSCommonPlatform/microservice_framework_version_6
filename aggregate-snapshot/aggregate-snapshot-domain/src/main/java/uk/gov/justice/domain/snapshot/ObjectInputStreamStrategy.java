package uk.gov.justice.domain.snapshot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public interface ObjectInputStreamStrategy {
    ObjectInputStream objectInputStreamOf(final ByteArrayInputStream bis) throws IOException;
}
