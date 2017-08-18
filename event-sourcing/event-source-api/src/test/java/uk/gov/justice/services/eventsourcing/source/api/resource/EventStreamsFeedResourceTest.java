package uk.gov.justice.services.eventsourcing.source.api.resource;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.api.feed.common.Feed;
import uk.gov.justice.services.eventsourcing.source.api.feed.common.Paging;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamsFeedService;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
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

        final UriInfo uriInfo = new ResteasyUriInfo("", "", "");
        final Feed feed = new Feed(emptyList(), new Paging("", ""));
        final String page = "1";

        when(service.feed(page, uriInfo)).thenReturn(feed);

        assertThat(resource.eventStreams(page, uriInfo), is(feed));

    }

    @Test
    public void shouldCheckAccessRights() throws Exception {

        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());
        resource.headers = requestHeaders;
        resource.eventStreams("1", new ResteasyUriInfo("", "", ""));

        verify(accessControlChecker).checkAccessControl(requestHeaders);

    }
}