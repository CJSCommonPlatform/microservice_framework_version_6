package uk.gov.justice.services.eventsourcing.source.api.feed.event;

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
public class EventEntryFeedMaxMinProviderProviderTest {

    private EventEntryFeedMaxMinProviderProvider eventEntryFeedMaxMinProviderProvider;

    private List<EventEntry> feed;

    @Before
    public void setup() {
        eventEntryFeedMaxMinProviderProvider = new EventEntryFeedMaxMinProviderProvider();

        feed = new ArrayList<>();
        feed.add(eventEntryWithSequence(0L));
        feed.add(eventEntryWithSequence(1L));
        feed.add(eventEntryWithSequence(2L));
        feed.add(eventEntryWithSequence(3L));
    }

    @Test
    public void shouldReturnZeroMinimumOnEmptyList() {
        final long min = eventEntryFeedMaxMinProviderProvider.min(EMPTY_LIST);

        assertThat(min, equalTo(0L));
    }

    @Test
    public void shouldReturnZeroMaximumOnEmptyList() {
        final long max = eventEntryFeedMaxMinProviderProvider.max(EMPTY_LIST);

        assertThat(max, equalTo(0L));
    }

    @Test
    public void shouldReturnMinimumValue() {

        final long min = eventEntryFeedMaxMinProviderProvider.min(feed);

        assertThat(min, equalTo(0L));
    }

    @Test
    public void shouldReturnMaximumValue() {

        final long max = eventEntryFeedMaxMinProviderProvider.max(feed);

        assertThat(max, equalTo(3L));
    }

    private EventEntry eventEntryWithSequence(final long sequenceId) {
        return new EventEntry(randomUUID(), randomUUID(), "name", sequenceId, null, "date");
    }
}