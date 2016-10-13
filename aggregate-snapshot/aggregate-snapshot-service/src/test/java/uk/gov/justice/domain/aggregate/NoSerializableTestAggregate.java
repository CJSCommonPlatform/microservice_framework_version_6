package uk.gov.justice.domain.aggregate;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.stream.Stream;

import org.apache.commons.lang.SerializationException;

public class NoSerializableTestAggregate implements Aggregate, Externalizable {

    private static final long serialVersionUID = 10000000001L;

    public NoSerializableTestAggregate() {
    }

    @Override
    public Object apply(Object event) {
        return null;
    }

    @Override
    public Stream<Object> apply(Stream<Object> events) {
        return null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        throw new SerializationException("Unable to Serialize the aggregate");
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new SerializationException("Unable to Serialize the aggregate");
    }
}
