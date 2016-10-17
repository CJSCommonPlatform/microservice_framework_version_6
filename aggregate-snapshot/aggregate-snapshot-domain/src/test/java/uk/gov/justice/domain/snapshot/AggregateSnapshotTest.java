package uk.gov.justice.domain.snapshot;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;


public class AggregateSnapshotTest {

    private final static long VERSION_ID = 1L;

    private final DefaultObjectInputStreamStrategy streamStrategy = new DefaultObjectInputStreamStrategy();
    private final UUID STREAM_ID = UUID.randomUUID();
    private final String TYPE = "uk.gov.justice.domain.snapshot.AggregateSnapshotTest$TestAggregate";

    @Test
    public void shouldCreateAnAggregateSnapshot() throws Exception {
        final TestAggregate aggregate = new TestAggregate("STATE1");

        final AggregateSnapshot<TestAggregate> snapshot = new AggregateSnapshot<>(STREAM_ID, VERSION_ID, aggregate);

        assertThat(snapshot.getStreamId(), is(STREAM_ID));
        assertThat(snapshot.getVersionId(), is(VERSION_ID));
        assertThat(snapshot.getType(), is(TYPE));
        assertThat(snapshot.getAggregateByteRepresentation(), is(SerializationUtils.serialize(aggregate)));
    }

    @Test
    public void shouldGetAnAggregateSnapshot() throws Exception {
        final TestAggregate aggregate = new TestAggregate("STATE1");

        final AggregateSnapshot<TestAggregate> snapshot = new AggregateSnapshot<>(STREAM_ID, VERSION_ID, aggregate);

        assertThat(snapshot.getStreamId(), is(STREAM_ID));
        assertThat(snapshot.getVersionId(), is(VERSION_ID));
        assertThat(snapshot.getType(), is(TYPE));
        assertThat(snapshot.getAggregate(streamStrategy), is(aggregate));
    }


    public static class TestAggregate implements Aggregate, Serializable {
        private static final long serialVersionUID = 42L;

        private final String name;

        public TestAggregate(String name) {
            this.name = name;
        }

        @Override
        public Object apply(Object event) {
            return event;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestAggregate that = (TestAggregate) o;

            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}