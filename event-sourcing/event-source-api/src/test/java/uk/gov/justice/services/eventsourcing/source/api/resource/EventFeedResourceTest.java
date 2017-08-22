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
        final Feed feed = new Feed(emptyList(), new Paging("", ""));
        final String page = "1";

        final Map<String, Object> params = new HashMap<>();
        params.put("STREAM_ID", streamId);

        when(service.feed(page, uriInfo, params)).thenReturn(feed);

        final Feed feedActual = resource.events(page, streamId, uriInfo);

        assertThat(feedActual, is(feed));

        final ArgumentCaptor<HashMap> paramsCaptor = ArgumentCaptor.forClass(HashMap.class);
        final ArgumentCaptor<String> pageCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<UriInfo> uriInfoCaptor = ArgumentCaptor.forClass(UriInfo.class);

        verify(service).feed(pageCaptor.capture(), uriInfoCaptor.capture(), paramsCaptor.capture());

        assertThat(pageCaptor.getValue(), is(page));

        assertThat(uriInfoCaptor.getValue(), is(uriInfo));

        assertThat(paramsCaptor.getValue().get("STREAM_ID"), is(streamId));

    }

    @Test
    public void shouldCheckAccessRights() throws Exception {
        final String streamId = randomUUID().toString();
        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());
        resource.headers = requestHeaders;
        resource.events("1", streamId, new ResteasyUriInfo("", "", ""));

        verify(accessControlChecker).checkAccessControl(requestHeaders);

    }
}