package uk.gov.justice.services.adapter.rest.multipart;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.adapter.rest.interceptor.FileStoreFailedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFileInputDetailsFactoryTest {

    @Mock
    private InputPartFileNameExtractor inputPartFileNameExtractor;

    @InjectMocks
    private DefaultFileInputDetailsFactory fileInputDetailsFactory;

    @Test
    public void shouldCreateFileInputDetailsFromFilePart() throws Exception {

        final String fileName = "the-file-name.jpeg";
        final String fieldName = "myFieldName";

        final MultipartFormDataInput multipartFormDataInput = mock(MultipartFormDataInput.class);
        final InputPart inputPart = mock(InputPart.class);
        final InputStream inputStream = mock(InputStream.class);

        final Map<String, List<InputPart>> formDataMap = ImmutableMap.of(fieldName, singletonList(inputPart));

        when(multipartFormDataInput.getFormDataMap()).thenReturn(formDataMap);
        when(inputPartFileNameExtractor.extractFileName(inputPart)).thenReturn(fileName);
        when(inputPart.getBody(InputStream.class, null)).thenReturn(inputStream);

        final List<FileInputDetails> fileInputDetails = fileInputDetailsFactory.createFileInputDetailsFrom(
                multipartFormDataInput,
                singletonList(fieldName));

        final FileInputDetails inputDetails = fileInputDetails.get(0);
        assertThat(inputDetails.getFileName(), is(fileName));
        assertThat(inputDetails.getFieldName(), is(fieldName));
        assertThat(inputDetails.getInputStream(), is(inputStream));
    }

    @Test
    public void shouldHandleMoreThanOneMultipart() throws Exception {

        final String fileName_1 = "the-file-name_1.jpeg";
        final String fieldName_1 = "myFieldName_1";

        final String fileName_2 = "the-file-name_2.jpeg";
        final String fieldName_2 = "myFieldName_2";

        final MultipartFormDataInput multipartFormDataInput = mock(MultipartFormDataInput.class);
        final InputPart inputPart_1 = mock(InputPart.class);
        final InputPart inputPart_2 = mock(InputPart.class);
        final InputStream inputStream_1 = mock(InputStream.class);
        final InputStream inputStream_2 = mock(InputStream.class);

        final Map<String, List<InputPart>> formDataMap = ImmutableMap.of(
                fieldName_1, singletonList(inputPart_1),
                fieldName_2, singletonList(inputPart_2)
        );

        when(multipartFormDataInput.getFormDataMap()).thenReturn(formDataMap);
        when(inputPartFileNameExtractor.extractFileName(inputPart_1)).thenReturn(fileName_1);
        when(inputPartFileNameExtractor.extractFileName(inputPart_2)).thenReturn(fileName_2);
        when(inputPart_1.getBody(InputStream.class, null)).thenReturn(inputStream_1);
        when(inputPart_2.getBody(InputStream.class, null)).thenReturn(inputStream_2);

        final List<FileInputDetails> fileInputDetails = fileInputDetailsFactory.createFileInputDetailsFrom(
                multipartFormDataInput,
                asList(fieldName_1, fieldName_2));

        final FileInputDetails inputDetails_1 = fileInputDetails.get(0);
        assertThat(inputDetails_1.getFileName(), is(fileName_1));
        assertThat(inputDetails_1.getFieldName(), is(fieldName_1));
        assertThat(inputDetails_1.getInputStream(), is(inputStream_1));

        final FileInputDetails inputDetails_2 = fileInputDetails.get(1);
        assertThat(inputDetails_2.getFileName(), is(fileName_2));
        assertThat(inputDetails_2.getFieldName(), is(fieldName_2));
        assertThat(inputDetails_2.getInputStream(), is(inputStream_2));
    }

    @Test
    public void shouldThrowFileStoreFailedExceptionIfGettingFileInputStreamFails() throws Exception {

        final IOException ioException = new IOException("bunnies");

        final String fileName = "the-file-name.jpeg";
        final String fieldName = "myFieldName";

        final MultipartFormDataInput multipartFormDataInput = mock(MultipartFormDataInput.class);
        final InputPart inputPart = mock(InputPart.class);

        final Map<String, List<InputPart>> formDataMap = ImmutableMap.of(fieldName, singletonList(inputPart));

        when(multipartFormDataInput.getFormDataMap()).thenReturn(formDataMap);
        when(inputPartFileNameExtractor.extractFileName(inputPart)).thenReturn(fileName);
        when(inputPart.getMediaType()).thenReturn(TEXT_XML_TYPE);
        when(inputPart.getBody(InputStream.class, null)).thenThrow(ioException);

        try {
            fileInputDetailsFactory.createFileInputDetailsFrom(
                    multipartFormDataInput,
                    singletonList(fieldName));
            fail();
        } catch (final FileStoreFailedException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is("Failed to store file 'the-file-name.jpeg'"));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionIfNoInputPartFoundWithTheNameSpecifiedInTheRaml() throws Exception {

        final String fileName = "the-file-name.jpeg";
        final String fieldName = "myFieldName";

        final MultipartFormDataInput multipartFormDataInput = mock(MultipartFormDataInput.class);
        final InputPart inputPart = mock(InputPart.class);
        final InputStream inputStream = mock(InputStream.class);

        final Map<String, List<InputPart>> formDataMap = new HashMap<>();

        when(multipartFormDataInput.getFormDataMap()).thenReturn(formDataMap);
        when(inputPartFileNameExtractor.extractFileName(inputPart)).thenReturn(fileName);
        when(inputPart.getBody(InputStream.class, null)).thenReturn(inputStream);

        try {
            fileInputDetailsFactory.createFileInputDetailsFrom(
                    multipartFormDataInput,
                    singletonList(fieldName));
            fail();
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Failed to find input part named 'myFieldName' as specified in the raml"));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionIfTheListOfFilePartsForAFieldNameIsEmpty() throws Exception {

        final String fileName = "the-file-name.jpeg";
        final String fieldName = "myFieldName";

        final MultipartFormDataInput multipartFormDataInput = mock(MultipartFormDataInput.class);
        final InputPart inputPart = mock(InputPart.class);
        final InputStream inputStream = mock(InputStream.class);

        final Map<String, List<InputPart>> formDataMap = ImmutableMap.of(fieldName, emptyList());

        when(multipartFormDataInput.getFormDataMap()).thenReturn(formDataMap);
        when(inputPartFileNameExtractor.extractFileName(inputPart)).thenReturn(fileName);
        when(inputPart.getBody(InputStream.class, null)).thenReturn(inputStream);

        try {
            fileInputDetailsFactory.createFileInputDetailsFrom(
                    multipartFormDataInput,
                    singletonList(fieldName));
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("The list of input parts named 'myFieldName' is empty"));
        }
    }
}
