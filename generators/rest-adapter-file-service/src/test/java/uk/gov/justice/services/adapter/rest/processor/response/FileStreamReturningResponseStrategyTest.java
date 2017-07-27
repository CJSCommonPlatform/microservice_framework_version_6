package uk.gov.justice.services.adapter.rest.processor.response;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.fileservice.api.FileRetriever;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.domain.FileReference;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class FileStreamReturningResponseStrategyTest {

    @Mock
    private FileRetriever fileRetriever;

    @Mock
    private Logger logger;

    private ResponseStrategyHelper responseStrategyHelper;

    @InjectMocks
    private FileStreamReturningResponseStrategy strategy;

    @Before
    public void setUp() throws Exception {
        responseStrategyHelper = new ResponseStrategyHelper();
        responseStrategyHelper.logger = logger;
        strategy.responseStrategyHelper = responseStrategyHelper;
    }

    @Test
    public void shouldReturnFileStreamInResponse() throws Exception {
        final UUID fileId = randomUUID();
        final InputStream fileStream = toInputStream("someFileContentABC");
        when(fileRetriever.retrieve(fileId)).thenReturn(Optional.of(new FileReference(fileId, null, fileStream, false)));

        final Response response = strategy.responseFor("someAction", Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf(fileId, "fileId").build()));

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(InputStream.class));
        assertThat(response.getEntity(), is(sameInstance((fileStream))));
    }


    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowExceptionIfFileServiceThrowsException() throws FileServiceException {

        final UUID fileId = randomUUID();
        when(fileRetriever.retrieve(fileId)).thenThrow(new FileServiceException(""));

        strategy.responseFor("someAction", Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf(fileId, "fileId").build()));

    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionIfFileNotFound() throws Exception {

        final UUID fileId = randomUUID();
        when(fileRetriever.retrieve(fileId)).thenReturn(empty());

        strategy.responseFor("someAction", Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf(fileId, "fileId").build()));

    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionIfPayloadNull() throws Exception {
        strategy.responseFor("someAction", Optional.of(envelope().with(metadataWithDefaults()).withNullPayload().build()));

    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowExceptionIfFileIdNotPresentInPayload() throws Exception {
        strategy.responseFor("someAction", Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf("", "someOtherField").build()));

    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowExceptionIfResultEmpty() throws Exception {
        strategy.responseFor("someAction", Optional.empty());
    }
}