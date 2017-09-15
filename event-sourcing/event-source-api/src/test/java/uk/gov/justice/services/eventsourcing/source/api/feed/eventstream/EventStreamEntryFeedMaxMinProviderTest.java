package uk.gov.justice.services.eventsourcing.source.api.feed.eventstream;

import static java.util.Collections.EMPTY_LIST;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EventStreamEntryFeedMaxMinProviderTest {

    private EventStreamEntryFeedMaxMinProvider eventStreamEntryFeedMaxMinProvider;

    private List<EventStreamEntry> feed;

    @Before
    public void setup() {
        eventStreamEntryFeedMaxMinProvider = new EventStreamEntryFeedMaxMinProvider();

        feed = new ArrayList<>();
        feed.add(eventStreamEntryWithSequence(0L));
        feed.add(eventStreamEntryWithSequence(1L));
        feed.add(eventStreamEntryWithSequence(2L));
        feed.add(eventStreamEntryWithSequence(3L));
    }

    @Test
    public void shouldReturnZeroMinimumOnEmptyList() {
        final long min = eventStreamEntryFeedMaxMinProvider.min(EMPTY_LIST);

        assertThat(min, equalTo(0L));
    }

    @Test
    public void shouldReturnZeroMaximumOnEmptyList() {
        final long max = eventStreamEntryFeedMaxMinProvider.max(EMPTY_LIST);

        assertThat(max, equalTo(0L));
    }

    @Test
    public void shouldReturnMinimumValue() {

        final long min = eventStreamEntryFeedMaxMinProvider.min(feed);

        assertThat(min, equalTo(0L));
    }

    @Test
    public void shouldReturnMaximumValue() {

        final long max = eventStreamEntryFeedMaxMinProvider.max(feed);

        assertThat(max, equalTo(3L));
    }

    private EventStreamEntry eventStreamEntryWithSequence(final long sequenceId) {
        return new EventStreamEntry(sequenceId, randomUUID(), "href");
    }
}