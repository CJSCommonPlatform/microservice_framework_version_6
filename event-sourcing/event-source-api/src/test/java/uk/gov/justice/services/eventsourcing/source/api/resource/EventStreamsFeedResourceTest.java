package uk.gov.justice.services.eventsourcing.source.api.resource;

import static java.util.Collections.emptyList;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Paging;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamsFeedService;
import uk.gov.justice.services.jdbc.persistence.Link;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamsFeedResourceTest {

    @Mock
    private EventStreamsFeedService service;

    @Mock
    private AccessController accessControlChecker;

    @InjectMocks
    private EventStreamsFeedResource resource;


    @Test
    public void shouldReturnFeedReturnedByService() throws Exception {
        final Link link = Link.PREVIOUS;

        final UriInfo uriInfo = new ResteasyUriInfo("", "", "");
        final Feed feed = new Feed(emptyList(), new Paging("", "", "", ""));
        final String page = "1";

        final Map<String, Object> params = new HashMap();
//        when(service.feed(page, link, uriInfo, params)).thenReturn(feed);
//
//        final Feed actualFeed = resource.eventStreams(page, link.name(), uriInfo);
//        assertThat(actualFeed, is(feed));

    }

    @Test
    public void shouldCheckAccessRights() throws Exception {
        final Link link = Link.PREVIOUS;

//        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());
//        resource.headers = requestHeaders;
//        resource.eventStreams("1", link.name(), new ResteasyUriInfo("", "", ""));
//
//        verify(accessControlChecker).checkAccessControl(requestHeaders);

    }
}