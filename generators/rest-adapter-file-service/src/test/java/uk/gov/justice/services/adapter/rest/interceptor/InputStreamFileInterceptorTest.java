package uk.gov.justice.services.adapter.rest.interceptor;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.adapter.rest.multipart.FileInputDetails.FILE_INPUT_DETAILS_LIST;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;
import uk.gov.justice.services.core.interceptor.DefaultInterceptorChain;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InputStreamFileInterceptorTest {

    @Mock
    private JsonEnvelope inputEnvelope;

    @Mock
    private JsonEnvelope outputEnvelope;

    @Mock
    private MultipleFileInputDetailsService multipleFileInputDetailsService;

    @Mock
    private ResultsHandler resultsHandler;

    private InterceptorChain interceptorChain;

    @InjectMocks
    private InputStreamFileInterceptor inputStreamFileInterceptor;

    private JsonEnvelope resultJsonEnvelope;

    @Before
    public void setup() throws Exception {
        final Deque<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(inputStreamFileInterceptor);

        interceptorChain = new DefaultInterceptorChain(interceptors, this::processResult);
    }

    @Test
    public void shouldStoreTheInputStreamInTheFileStoreAndUpdateTheInputEnvelope() throws Exception {

        final FileInputDetails fileInputDetails = mock(FileInputDetails.class);
        final List<FileInputDetails> fileInputDetailsList = singletonList(fileInputDetails);
        final Map<String, UUID> results = ImmutableMap.of("fieldName", randomUUID());

        final InterceptorContext inputInterceptorContext = createInterceptorContext(fileInputDetailsList);

        when(multipleFileInputDetailsService.storeFileDetails(fileInputDetailsList)).thenReturn(results);

        when(resultsHandler.addResultsTo(inputEnvelope, results)).thenReturn(inputEnvelope);

        final InterceptorContext outputInterceptorContext = interceptorChain.processNext(inputInterceptorContext);

        final Optional<JsonEnvelope> jsonEnvelope = outputInterceptorContext.outputEnvelope();

        assertThat(jsonEnvelope.isPresent(), is(true));
        assertThat(jsonEnvelope.get(), is(outputEnvelope));

        assertThat(resultJsonEnvelope, is(inputEnvelope));
    }

    @Test
    public void shouldNotStoreTheInputStreamIfNoFileDetailsFoundInTheInterceptorContext() throws Exception {

        final InterceptorContext inputInterceptorContext = createEmptyInterceptorContext();

        final InterceptorContext outputInterceptorContext = interceptorChain.processNext(inputInterceptorContext);

        final Optional<JsonEnvelope> jsonEnvelope = outputInterceptorContext.outputEnvelope();

        assertThat(jsonEnvelope.isPresent(), is(true));
        assertThat(jsonEnvelope.get(), is(outputEnvelope));

        assertThat(resultJsonEnvelope, is(inputEnvelope));

        verifyZeroInteractions(multipleFileInputDetailsService);
    }

    private InterceptorContext createInterceptorContext(final List<FileInputDetails> fileInputDetails) {
        final InterceptorContext inputInterceptorContext = interceptorContextWithInput(inputEnvelope);
        inputInterceptorContext.setInputParameter(FILE_INPUT_DETAILS_LIST, fileInputDetails);
        return inputInterceptorContext;
    }

    private InterceptorContext createEmptyInterceptorContext() {
        return interceptorContextWithInput(inputEnvelope);
    }

    private InterceptorContext processResult(final InterceptorContext interceptorContext) {
        resultJsonEnvelope = interceptorContext.inputEnvelope();
        return interceptorContext.copyWithOutput(outputEnvelope);
    }
}
