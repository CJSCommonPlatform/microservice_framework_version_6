package uk.gov.justice.services.eventsourcing.source.api.resource;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.PagingLinks.PagingLinksBuilder.pagingLinksBuilder;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamPageEntry;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamPageService;
import uk.gov.justice.services.eventsourcing.source.api.service.Page;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;

import java.net.URL;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
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
public class EventStreamPageResourceTest {

    @Mock
    private EventStreamPageService eventsStreamPageService;

    @Mock
    private PositionFactory positionFactory;

    @Mock
    private AccessController accessControlChecker;

    @Mock
    private ObjectToJsonValueConverter converter;

    @InjectMocks
    private EventStreamPageResource resource;

    private static final int PAGE_SIZE = 10;

    @Test
    public void shouldReturnFeedReturnedByService() throws Exception {

        final UUID streamId = randomUUID();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page<EventStreamPageEntry> page = new Page<>(emptyList(), pagingLinksBuilder(fixedUrl, fixedUrl).build());

        final String position = FIRST;

        when(eventsStreamPageService.pageOfEventStream(position, FORWARD, PAGE_SIZE, uriInfo)).thenReturn(page);

        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("key", "value");

        when(converter.convert(page)).thenReturn(jsonObjectBuilder.build());

        resource.events(FIRST, FORWARD.toString(), PAGE_SIZE, uriInfo);

        final ArgumentCaptor<String> positionCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<UriInfo> uriInfoCaptor = ArgumentCaptor.forClass(UriInfo.class);
        final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);

        verify(eventsStreamPageService).pageOfEventStream(positionCaptor.capture(), directionCaptor.capture(), pageSizeCaptor.capture(), uriInfoCaptor.capture());

        assertThat(positionCaptor.getValue(), is(position));

        assertThat(pageSizeCaptor.getValue(), is(PAGE_SIZE));

        assertThat(uriInfoCaptor.getValue(), is(uriInfo));

        assertThat(directionCaptor.getValue(), is(FORWARD));

    }

    @Test(expected = BadRequestException.class)
    public void shouldReturnBadRequestWhenHeadEventsRequestedWithForwardDirection() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page<EventStreamPageEntry> page = new Page<>(emptyList(), pagingLinksBuilder(fixedUrl, fixedUrl).build());

        when(eventsStreamPageService.pageOfEventStream(HEAD, FORWARD, PAGE_SIZE, uriInfo)).thenReturn(page);

        resource.events(HEAD, FORWARD.toString(), PAGE_SIZE, uriInfo);
    }

    @Test(expected = BadRequestException.class)
    public void shouldReturnBadRequestWhenFirstEventsRequestedWithBackwardDirection() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page<EventStreamPageEntry> page = new Page<>(emptyList(), pagingLinksBuilder(fixedUrl, fixedUrl).build());

        when(eventsStreamPageService.pageOfEventStream(FIRST, BACKWARD, PAGE_SIZE, uriInfo)).thenReturn(page);

        resource.events(FIRST, BACKWARD.toString(), PAGE_SIZE, uriInfo);
    }

    @Test
    public void shouldCheckAccessRights() throws Exception {

        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());

        resource.headers = requestHeaders;
        resource.events("3", FORWARD.toString(), PAGE_SIZE, new ResteasyUriInfo("", "", ""));

        verify(accessControlChecker).checkAccessControl(requestHeaders);
    }
}