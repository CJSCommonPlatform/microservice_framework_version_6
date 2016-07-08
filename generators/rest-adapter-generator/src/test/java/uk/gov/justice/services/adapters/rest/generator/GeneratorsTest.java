package uk.gov.justice.services.adapters.rest.generator;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.generators.test.utils.builder.RamlBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.MimeType;
import org.raml.model.Raml;

public class GeneratorsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Generators.class);
    }

    @Test
    public void shouldReturnComponentFromBaseUriForCommandApi() throws Exception {
        Raml raml = restRamlWithCommandApiDefaults().build();
        Component component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(COMMAND_API));
    }

    @Test
    public void shouldReturnComponentFromBaseUriForEventApi() throws Exception {
        Raml raml = RamlBuilder.restRamlWithEventApiDefaults().build();
        Component component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(EVENT_API));
    }

    @Test
    public void shouldReturnComponentFromBaseUriForQueryApi() throws Exception {
        Raml raml = restRamlWithQueryApiDefaults().build();
        Component component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(QUERY_API));
    }

    @Test
    public void shouldThrowExceptionIfNoValidPillarAndTier() throws Exception {
        Raml raml = new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/event/listener/rest/service").build();

        exception.expect(IllegalStateException.class);
        exception.expectMessage("Base URI must contain valid pillar and tier: http://localhost:8080/warname/event/listener/rest/service");

        Generators.componentFromBaseUriIn(raml);
    }

    @Test
    public void shouldSortMimeTypes() throws Exception {
        MimeType mimeTypeA = new MimeType("application/vnd.a+json");
        MimeType mimeTypeB = new MimeType("application/vnd.b+json");
        MimeType mimeTypeC = new MimeType("application/vnd.c+json");
        List<MimeType> mimeTypes = Arrays.asList(mimeTypeB, mimeTypeC, mimeTypeA);

        assertThat(mimeTypes, contains(mimeTypeB, mimeTypeC, mimeTypeA));

        List<MimeType> orderedMimeTypes = mimeTypes.stream()
                .sorted(Generators.byMimeTypeOrder())
                .collect(Collectors.toList());

        assertThat(orderedMimeTypes, contains(mimeTypeA, mimeTypeB, mimeTypeC));
    }

}