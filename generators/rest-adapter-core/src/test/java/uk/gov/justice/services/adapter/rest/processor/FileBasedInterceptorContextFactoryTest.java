package uk.gov.justice.services.adapter.rest.processor;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.adapter.rest.multipart.FileInputDetails.FILE_INPUT_DETAILS_LIST;

import uk.gov.justice.services.adapter.rest.multipart.FileBasedInterceptorContextFactory;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileBasedInterceptorContextFactoryTest {

    @InjectMocks
    private FileBasedInterceptorContextFactory fileBasedInterceptorContextFactory;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateAnInterceptorContextContainingTheFileDetailsList() throws Exception {

        final JsonEnvelope inputEnvelope = mock(JsonEnvelope.class);
        final FileInputDetails fileInputDetails_1 = mock(FileInputDetails.class);
        final FileInputDetails fileInputDetails_2 = mock(FileInputDetails.class);

        final List<FileInputDetails> fileInputDetailss = asList(fileInputDetails_1, fileInputDetails_2);

        final InterceptorContext interceptorContext = fileBasedInterceptorContextFactory.create(fileInputDetailss, inputEnvelope);

        assertThat(interceptorContext.inputEnvelope(), is(inputEnvelope));
        final Optional<Object> inputParameter = interceptorContext.getInputParameter(FILE_INPUT_DETAILS_LIST);

        assertThat(inputParameter.isPresent(), is(true));

        final List<FileInputDetails> fileInputDetailsList = (List<FileInputDetails>) inputParameter.get();

        assertThat(fileInputDetailsList.size(), is(2));
        assertThat(fileInputDetailsList, hasItem(fileInputDetails_1));
        assertThat(fileInputDetailsList, hasItem(fileInputDetails_2));
    }
}