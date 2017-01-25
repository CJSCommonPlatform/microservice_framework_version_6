package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.builder.ResponseBuilder.response;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.firstMethodOf;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RestAdapterGenerator_SynchronousPOSTMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withHttpActionOfDefaultRequestAndResponseType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        final Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        final Object resourceObject = getInstanceOf(resourceClass);

        final Response processorResponse = Response.ok().build();
        when(restProcessor.processSynchronously(any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class), any(Collection.class))).thenReturn(processorResponse);

        final Method method = firstMethodOf(resourceClass);

        final Object result = method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        assertThat(result, is(processorResponse));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldThrowExceptionIfAcceptedStatusResponseWithNoBody() throws Exception {
        expectedException.expect(RamlValidationException.class);
        expectedException.expectMessage("Request type not set");

        final Map<String, org.raml.model.Response> responses = new HashMap<>();
        responses.put(valueOf(ACCEPTED.getStatusCode()), response().build());

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST).withResponsesFrom(responses))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }
}
