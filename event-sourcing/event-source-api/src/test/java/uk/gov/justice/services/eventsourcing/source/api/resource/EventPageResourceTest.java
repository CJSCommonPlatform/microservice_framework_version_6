package uk.gov.justice.services.eventsourcing.source.api.resource;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.FIRST;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.FixedPosition.HEAD;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Page;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.PagingLinks;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsPageService;

import java.net.URL;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventPageResourceTest {

    @Mock
    private EventsPageService eventsPageService;

    @Mock
    private AccessController accessControlChecker;

    @Mock
    private ObjectToJsonValueConverter converter;

    @InjectMocks
    private EventPageResource resource;

    @Test
    public void shouldReturnFeedReturnedByService() throws Exception {

        final UUID streamId = randomUUID();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page page = new Page(emptyList(), new PagingLinks.PagingLinksBuilder(fixedUrl, fixedUrl).build());

        final long pageSize = 10L;

        final String position = "1";

        when(eventsPageService.pageEvents(streamId, position, FORWARD, pageSize, uriInfo)).thenReturn(page);

        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("key", "value");

        when(converter.convert(page)).thenReturn(jsonObjectBuilder.build());

        resource.events(streamId.toString(), position, FORWARD.toString(), pageSize, uriInfo);

        final ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<String> positionCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Long> pageSizeCaptor = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor<UriInfo> uriInfoCaptor = ArgumentCaptor.forClass(UriInfo.class);
        final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);

        verify(eventsPageService).pageEvents(uuidArgumentCaptor.capture(), positionCaptor.capture(), directionCaptor.capture(), pageSizeCaptor.capture(), uriInfoCaptor.capture());

        assertThat(positionCaptor.getValue(), is(position));

        assertThat(pageSizeCaptor.getValue(), is(pageSize));

        assertThat(uriInfoCaptor.getValue(), is(uriInfo));

        assertThat(directionCaptor.getValue(), is(FORWARD));

        assertThat(uuidArgumentCaptor.getValue(), is(streamId));
    }

    @Test(expected = BadRequestException.class)
    public void shouldReturnBadRequestWhenHeadEventsRequestedWithForwardDirection() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page page = new Page(emptyList(), new PagingLinks.PagingLinksBuilder(fixedUrl, fixedUrl).build());

        when(eventsPageService.pageEvents(UUID.fromString(streamId), HEAD.getPosition(), FORWARD, 10L, uriInfo)).thenReturn(page);

        resource.events(streamId, HEAD.getPosition(), FORWARD.toString(), 10L, uriInfo);
    }

    @Test(expected = BadRequestException.class)
    public void shouldReturnBadRequestWhenFirstEventsRequestedWithBackwardDirection() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page page = new Page(emptyList(), new PagingLinks.PagingLinksBuilder(fixedUrl, fixedUrl).build());

        when(eventsPageService.pageEvents(UUID.fromString(streamId), FIRST.getPosition(), BACKWARD, 10L, uriInfo)).thenReturn(page);

        resource.events(streamId, FIRST.getPosition(), BACKWARD.toString(), 10L, uriInfo);
    }

    @Test
    public void shouldCheckAccessRights() throws Exception {

        final String streamId = randomUUID().toString();

        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());

        resource.headers = requestHeaders;
        resource.events(streamId, "1", FORWARD.toString(), 1, new ResteasyUriInfo("", "", ""));

        verify(accessControlChecker).checkAccessControl(requestHeaders);
    }
}