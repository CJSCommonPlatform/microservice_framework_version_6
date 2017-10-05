package uk.gov.justice.services.adapters.rest.generator;

import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MimeTypeBuilder.multipartWithFileFormParameter;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.junit.Test;
import org.mockito.Mock;

public class RestAdapterGenerator_MultipartMethodBodyTest extends BaseRestAdapterGeneratorTest {

    @Mock
    private List<FileInputDetails> fileInputDetails;

    @Mock
    private MultipartInput multipartInput;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/some/path")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId"))
                                        .with(mapping()
                                                .withName("upload")
                                                .withRequestType(MULTIPART_FORM_DATA)))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultCommandApiSomePathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Response processorResponse = Response.ok().build();
        when(restProcessor.process(anyString(), any(Function.class), anyString(), any(HttpHeaders.class),
                any(Collection.class), any(List.class))).thenReturn(processorResponse);

        final Method method = firstMethodOf(resourceClass);

        final Object result = method.invoke(resourceObject, mock(MultipartFormDataInput.class));

        assertThat(result, is(processorResponse));
    }
}
