package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.eventstream.EventStreamEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamsFeedServiceTest {

    private static final ResteasyUriInfo NOT_USED_URI_INFO = uriInfoWithAbsoluteUri("http://server:222/context/streams");
    @Mock
    private EventStreamJdbcRepository repository;

    @InjectMocks
    private EventStreamsFeedService service;

    private static ResteasyUriInfo uriInfoWithAbsoluteUri(final String absoluteUri) {
        return new ResteasyUriInfo(absoluteUri, "", "");
    }

    @Test
    public void shouldReturnFirstPageWhenMoreRecordsThanPageSize() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();

        final Map<String, Object> params = new HashMap();

        initialiseWithPageSize(service, 2L);

        when(repository.getPage(0, 3, params)).thenReturn(Stream.of(new EventStream(streamId1), new EventStream(streamId2), new EventStream(randomUUID())));

        final Feed<EventStreamEntry> feed = service.feed("1", uriInfoWithAbsoluteUri("http://server:123/context/streams"), params);

        final List<EventStreamEntry> streamData = feed.getData();

        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId1.toString()));
        assertThat(streamData.get(0).getHref(), is("http://server:123/context/streams/" + streamId1.toString()));

        assertThat(streamData.get(1).getStreamId(), is(streamId2.toString()));
        assertThat(streamData.get(1).getHref(), is("http://server:123/context/streams/" + streamId2.toString()));

    }

    @Test
    public void shouldReturnFirstPageWhenLessRecordsThanPageSize() throws Exception {

        final UUID streamId1 = randomUUID();

        initialiseWithPageSize(service, 2L);

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(0, 3, params)).thenReturn(Stream.of(new EventStream(streamId1)));

        final Feed<EventStreamEntry> feed = service.feed("1", NOT_USED_URI_INFO, params);

        final List<EventStreamEntry> streamData = feed.getData();

        assertThat(streamData, hasSize(1));

        assertThat(streamData.get(0).getStreamId(), is(streamId1.toString()));

    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSize() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();

        initialiseWithPageSize(service, 2L);

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(0, 3, params)).thenReturn(Stream.of(new EventStream(streamId1), new EventStream(streamId2)));

        final Feed<EventStreamEntry> feed = service.feed("1", NOT_USED_URI_INFO, params);

        final List<EventStreamEntry> streamData = feed.getData();


        assertThat(streamData, hasSize(2));

        assertThat(streamData.get(0).getStreamId(), is(streamId1.toString()));
        assertThat(streamData.get(1).getStreamId(), is(streamId2.toString()));

    }

    @Test
    public void shouldReturnLinkTo2ndPage() throws Exception {

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(0, 3, params)).thenReturn(Stream.of(new EventStream(randomUUID(), 1L), new EventStream(randomUUID(), 2L), new EventStream(randomUUID(), 3L)));

        initialiseWithPageSize(service, 2L);

        final Feed<EventStreamEntry> feed = service.feed("1", uriInfoWithAbsoluteUri("http://server:123/context/streams"), params);

        assertThat(feed.getPaging().getNext(), is("http://server:123/context/streams?page=2"));

    }

    @Test
    public void shouldNotReturnLinkToNextPageIfNoMoreRecords() {

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(0, 3, params)).thenReturn(Stream.of(new EventStream(randomUUID(), 1L), new EventStream(randomUUID(), 2L)));

        initialiseWithPageSize(service, 2L);

        final Feed<EventStreamEntry> feed = service.feed("1", NOT_USED_URI_INFO, params);

        assertThat(feed.getPaging().getNext(), is(nullValue()));

    }

    @Test
    public void shouldReturnLinkToPreviousPage() throws Exception {

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(2, 3, params)).thenReturn(Stream.of(new EventStream(randomUUID(), 1L), new EventStream(randomUUID(), 2L), new EventStream(randomUUID(), 3L)));

        initialiseWithPageSize(service, 2L);

        final Feed<EventStreamEntry> feed = service.feed("2", uriInfoWithAbsoluteUri("http://server:234/context/streams"), params);

        assertThat(feed.getPaging().getPrevious(), is("http://server:234/context/streams?page=1"));

    }

    @Test
    public void shouldNotReturnLinkToPreviousPageIfOnFirstPage() throws Exception {

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(0, 3, params)).thenReturn(Stream.of(new EventStream(randomUUID(), 1L), new EventStream(randomUUID(), 2L), new EventStream(randomUUID(), 3L)));

        initialiseWithPageSize(service, 2L);

        final Feed<EventStreamEntry> feed = service.feed("1", uriInfoWithAbsoluteUri("http://server:234/context/streams"), params);

        assertThat(feed.getPaging().getPrevious(), is(nullValue()));

    }

    @Test
    public void shouldQueryFor3rdPage() throws Exception {

        final Map<String, Object> params = new HashMap();

        when(repository.getPage(anyInt(), anyInt(), anyMap())).thenReturn(Stream.empty());

        initialiseWithPageSize(service, 10L);

        service.feed("3", NOT_USED_URI_INFO, params);

        final ArgumentCaptor<HashMap> paramsCaptor = ArgumentCaptor.forClass(HashMap.class);
        final ArgumentCaptor<Long> offsetCaptor = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor<Long> pageCaptor = ArgumentCaptor.forClass(Long.class);

        verify(repository).getPage(pageCaptor.capture(), offsetCaptor.capture(), paramsCaptor.capture());

        assertThat(pageCaptor.getValue(), is(20L));

        assertThat(offsetCaptor.getValue(), is(11L));
    }

    private void initialiseWithPageSize(final EventStreamsFeedService service, final long pageSize) {
        service.pageSize = pageSize;
        service.initialise();
    }

}