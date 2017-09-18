package uk.gov.justice.services.eventsourcing.source.api.resource;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Paging;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsFeedService;
import uk.gov.justice.services.jdbc.persistence.Link;

import java.util.HashMap;
import java.util.Map;

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
public class EventFeedResourceTest {

    @Mock
    private EventsFeedService service;

    @Mock
    private AccessController accessControlChecker;

    @InjectMocks
    private EventFeedResource resource;


    @Test
    public void shouldReturnFeedReturnedByService() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final Feed feed = new Feed(emptyList(), new Paging("", "", "", ""));

        final long pageSize = 10L;

        final long offset = 1L;

        final Link link = Link.PREVIOUS;

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        when(service.feed(offset, link, pageSize, uriInfo, params)).thenReturn(feed);

        final Feed feedActual = resource.events(streamId, offset, link.name(), pageSize, uriInfo);

        assertThat(feedActual, is(feed));

        final ArgumentCaptor<HashMap> paramsCaptor = ArgumentCaptor.forClass(HashMap.class);
        final ArgumentCaptor<Long> offsetCaptor = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor<Long> pageSizeCaptor = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor<UriInfo> uriInfoCaptor = ArgumentCaptor.forClass(UriInfo.class);
        final ArgumentCaptor<Link> directionCaptor = ArgumentCaptor.forClass(Link.class);

        verify(service).feed(offsetCaptor.capture(), directionCaptor.capture(), pageSizeCaptor.capture(), uriInfoCaptor.capture(), paramsCaptor.capture());

        assertThat(offsetCaptor.getValue(), is(offset));

        assertThat(pageSizeCaptor.getValue(), is(pageSize));

        assertThat(uriInfoCaptor.getValue(), is(uriInfo));

        assertThat(directionCaptor.getValue(), is(link));

        assertThat(paramsCaptor.getValue().get("STREAM_ID"), is(streamId));
    }

    @Test
    public void shouldCheckAccessRights() throws Exception {
        final String streamId = randomUUID().toString();
        final Link link = Link.PREVIOUS;

        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());
        resource.headers = requestHeaders;
        resource.events(streamId, 1, link.name(), 1, new ResteasyUriInfo("", "", ""));

        verify(accessControlChecker).checkAccessControl(requestHeaders);

    }
}