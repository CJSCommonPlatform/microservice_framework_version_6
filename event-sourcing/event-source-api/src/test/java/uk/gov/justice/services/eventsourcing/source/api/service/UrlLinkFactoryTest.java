package uk.gov.justice.services.eventsourcing.source.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;

import uk.gov.justice.services.eventsourcing.source.api.service.core.Position;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionValueFactory;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UrlLinkFactoryTest {

    @Mock
    private PositionValueFactory positionValueFactory;

    @InjectMocks
    private UrlLinkFactory urlLinkFactory;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEventsHeadUrlLink() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final String eventStreamPathSegmentValue = "eventstreams";
        final String baseURI = "http://localhost:8080/context/";
        final String urlString = baseURI + streamId + "/HEAD/BACKWARD/2";
        final int pageSize = 2;

        final UriInfo uriInfo = mock(UriInfo.class);
        final UriBuilder uriBuilder = mock(UriBuilder.class);
        final List<PathSegment> pathSegmentList = mock(List.class);
        final PathSegment eventStreamPathSegment = mock(PathSegment.class);
        final PathSegment streamIdPathSegment = mock(PathSegment.class);

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        when(uriInfo.getPathSegments()).thenReturn(pathSegmentList);

        when(pathSegmentList.get(0)).thenReturn(eventStreamPathSegment);
        when(eventStreamPathSegment.getPath()).thenReturn(eventStreamPathSegmentValue);

        when(pathSegmentList.get(1)).thenReturn(streamIdPathSegment);
        when(streamIdPathSegment.getPath()).thenReturn(streamId.toString());

        when(uriBuilder.path(eventStreamPathSegmentValue)).thenReturn(uriBuilder);
        when(uriBuilder.path(streamId.toString())).thenReturn(uriBuilder);
        when(uriBuilder.path(HEAD)).thenReturn(uriBuilder);
        when(uriBuilder.path("BACKWARD")).thenReturn(uriBuilder);
        when(uriBuilder.path("2")).thenReturn(uriBuilder);

        when(uriBuilder.build()).thenReturn(new URL(urlString).toURI());

        when(positionValueFactory.getPositionValue(head())).thenReturn(HEAD);
        final URL link = urlLinkFactory.createHeadEventsUrlLink(pageSize, uriInfo);
        assertThat(link.toString(), is(urlString));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEventsFirstUrlLink() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final String eventStreamPathSegmentValue = "eventstreams";
        final String baseURI = "http://localhost:8080/context/";
        final String urlString = baseURI + streamId + "/1/FORWARD/2";
        final int pageSize = 2;

        final UriInfo uriInfo = mock(UriInfo.class);
        final UriBuilder uriBuilder = mock(UriBuilder.class);
        final List<PathSegment> pathSegmentList = mock(List.class);
        final PathSegment eventStreamPathSegment = mock(PathSegment.class);
        final PathSegment streamIdPathSegment = mock(PathSegment.class);

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        when(uriInfo.getPathSegments()).thenReturn(pathSegmentList);

        when(pathSegmentList.get(0)).thenReturn(eventStreamPathSegment);
        when(eventStreamPathSegment.getPath()).thenReturn(eventStreamPathSegmentValue);

        when(pathSegmentList.get(1)).thenReturn(streamIdPathSegment);
        when(streamIdPathSegment.getPath()).thenReturn(streamId.toString());

        when(uriBuilder.path(eventStreamPathSegmentValue)).thenReturn(uriBuilder);
        when(uriBuilder.path(streamId.toString())).thenReturn(uriBuilder);
        when(uriBuilder.path(FIRST)).thenReturn(uriBuilder);
        when(uriBuilder.path("FORWARD")).thenReturn(uriBuilder);
        when(uriBuilder.path("2")).thenReturn(uriBuilder);

        when(uriBuilder.build()).thenReturn(new URL(urlString).toURI());
        when(positionValueFactory.getPositionValue(first())).thenReturn(FIRST);

        final URL link = urlLinkFactory.createFirstEventsUrlLink(pageSize, uriInfo);

        assertThat(link.toString(), is(urlString));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEventStreamSelfUrlLink() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final String eventStreamPathSegmentValue = "eventstreams";
        final String baseURI = "http://localhost:8080/context/";
        final String urlString = baseURI + streamId + "/HEAD/BACKWARD/2";
        final int pageSize = 2;
        final Position position = head();

        final UriInfo uriInfo = mock(UriInfo.class);
        final UriBuilder uriBuilder = mock(UriBuilder.class);
        final List<PathSegment> pathSegmentList = mock(List.class);
        final PathSegment eventStreamPathSegment = mock(PathSegment.class);
        final PathSegment streamIdPathSegment = mock(PathSegment.class);

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        when(uriInfo.getPathSegments()).thenReturn(pathSegmentList);

        when(pathSegmentList.get(0)).thenReturn(eventStreamPathSegment);
        when(eventStreamPathSegment.getPath()).thenReturn(eventStreamPathSegmentValue);

        when(pathSegmentList.get(1)).thenReturn(streamIdPathSegment);
        when(streamIdPathSegment.getPath()).thenReturn(streamId.toString());

        when(uriBuilder.path(eventStreamPathSegmentValue)).thenReturn(uriBuilder);
        when(uriBuilder.path(streamId.toString())).thenReturn(uriBuilder);
        when(uriBuilder.path(HEAD)).thenReturn(uriBuilder);
        when(uriBuilder.path("BACKWARD")).thenReturn(uriBuilder);

        when(uriBuilder.path("2")).thenReturn(uriBuilder);

        when(uriBuilder.build()).thenReturn(new URL(urlString).toURI());
        when(positionValueFactory.getPositionValue(position)).thenReturn(HEAD);

        final URL link = urlLinkFactory.createEventStreamSelfUrlLink(streamId.toString(), pageSize, uriInfo);

        assertThat(link.toString(), is(urlString));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEventStreamsHeadUrlLink() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final String eventStreamPathSegmentValue = "eventstreams";
        final String baseURI = "http://localhost:8080/context/";
        final String urlString = baseURI + "/HEAD/BACKWARD/2";
        final int pageSize = 2;

        final UriInfo uriInfo = mock(UriInfo.class);
        final UriBuilder uriBuilder = mock(UriBuilder.class);
        final List<PathSegment> pathSegmentList = mock(List.class);
        final PathSegment eventStreamPathSegment = mock(PathSegment.class);
        final PathSegment streamIdPathSegment = mock(PathSegment.class);

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        when(uriInfo.getPathSegments()).thenReturn(pathSegmentList);

        when(pathSegmentList.get(0)).thenReturn(eventStreamPathSegment);
        when(eventStreamPathSegment.getPath()).thenReturn(eventStreamPathSegmentValue);

        when(pathSegmentList.get(1)).thenReturn(streamIdPathSegment);
        when(streamIdPathSegment.getPath()).thenReturn(streamId.toString());

        when(uriBuilder.path(eventStreamPathSegmentValue)).thenReturn(uriBuilder);
        when(uriBuilder.path(streamId.toString())).thenReturn(uriBuilder);
        when(uriBuilder.path(HEAD)).thenReturn(uriBuilder);
        when(uriBuilder.path("BACKWARD")).thenReturn(uriBuilder);
        when(uriBuilder.path("2")).thenReturn(uriBuilder);

        when(uriBuilder.build()).thenReturn(new URL(urlString).toURI());

        when(positionValueFactory.getPositionValue(head())).thenReturn(HEAD);
        final URL link = urlLinkFactory.createHeadEventStreamsUrlLink(pageSize, uriInfo);
        assertThat(link.toString(), is(urlString));
    }
    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEventStreamsFirstUrlLink() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final String eventStreamPathSegmentValue = "eventstreams";
        final String baseURI = "http://localhost:8080/context/";
        final String urlString = baseURI  + "/1/FORWARD/2";
        final int pageSize = 2;

        final UriInfo uriInfo = mock(UriInfo.class);
        final UriBuilder uriBuilder = mock(UriBuilder.class);
        final List<PathSegment> pathSegmentList = mock(List.class);
        final PathSegment eventStreamPathSegment = mock(PathSegment.class);
        final PathSegment streamIdPathSegment = mock(PathSegment.class);

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);

        when(uriInfo.getPathSegments()).thenReturn(pathSegmentList);

        when(pathSegmentList.get(0)).thenReturn(eventStreamPathSegment);
        when(eventStreamPathSegment.getPath()).thenReturn(eventStreamPathSegmentValue);

        when(pathSegmentList.get(1)).thenReturn(streamIdPathSegment);
        when(streamIdPathSegment.getPath()).thenReturn(streamId.toString());

        when(uriBuilder.path(eventStreamPathSegmentValue)).thenReturn(uriBuilder);
        when(uriBuilder.path(streamId.toString())).thenReturn(uriBuilder);
        when(uriBuilder.path(FIRST)).thenReturn(uriBuilder);
        when(uriBuilder.path("FORWARD")).thenReturn(uriBuilder);
        when(uriBuilder.path("2")).thenReturn(uriBuilder);

        when(uriBuilder.build()).thenReturn(new URL(urlString).toURI());
        when(positionValueFactory.getPositionValue(first())).thenReturn(FIRST);

        final URL link = urlLinkFactory.createFirstEventStreamsUrlLink(pageSize, uriInfo);

        assertThat(link.toString(), is(urlString));
    }

}