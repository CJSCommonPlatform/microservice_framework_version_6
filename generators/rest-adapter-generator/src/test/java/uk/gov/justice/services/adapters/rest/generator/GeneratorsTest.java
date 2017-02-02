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

import uk.gov.justice.services.generators.test.utils.builder.RamlBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        final Raml raml = restRamlWithCommandApiDefaults().build();
        final Optional<String> component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(Optional.of(COMMAND_API)));
    }

    @Test
    public void shouldReturnComponentFromBaseUriForEventApi() throws Exception {
        final Raml raml = RamlBuilder.restRamlWithEventApiDefaults().build();
        final Optional<String> component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(Optional.of(EVENT_API)));
    }

    @Test
    public void shouldReturnComponentFromBaseUriForQueryApi() throws Exception {
        final Raml raml = restRamlWithQueryApiDefaults().build();
        final Optional<String> component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(Optional.of(QUERY_API)));
    }

    @Test
    public void shouldReturnOptionalEmptyIfNoValidPillarAndTier() throws Exception {
        final Raml raml = new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/event/listener/rest/service").build();

        final Optional<String> component = Generators.componentFromBaseUriIn(raml);
        assertThat(component, is(Optional.empty()));
    }

    @Test
    public void shouldSortMimeTypes() throws Exception {
        final MimeType mimeTypeA = new MimeType("application/vnd.a+json");
        final MimeType mimeTypeB = new MimeType("application/vnd.b+json");
        final MimeType mimeTypeC = new MimeType("application/vnd.c+json");
        final List<MimeType> mimeTypes = Arrays.asList(mimeTypeB, mimeTypeC, mimeTypeA);

        assertThat(mimeTypes, contains(mimeTypeB, mimeTypeC, mimeTypeA));

        final List<MimeType> orderedMimeTypes = mimeTypes.stream()
                .sorted(Generators.byMimeTypeOrder())
                .collect(Collectors.toList());

        assertThat(orderedMimeTypes, contains(mimeTypeA, mimeTypeB, mimeTypeC));
    }
}