package uk.gov.justice.services.adapters.rest.generator;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapters.rest.generator.Generators.resourceInterfaceNameOf;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.services.adapters.rest.uri.BaseUri;
import uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public class GeneratorsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Generators.class);
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

    @Test
    public void shouldReturnInterfaceNamePrefixedWithComponentName() throws Exception {
        final Resource resource = resource("/abc").withDefaultPostAction().build();
        final String interfaceName = resourceInterfaceNameOf(resource, new BaseUri("http://localhost:8080/warname/command/api/rest/service"));
        assertThat(interfaceName, is("CommandApiAbcResource"));
    }

    @Test
    public void shouldReturnInterfaceNamePrefixedWithBaseUriPath() throws Exception {
        final Resource resource = resource("/bcd").withDefaultPostAction().build();
        final String interfaceName = resourceInterfaceNameOf(resource, new BaseUri("http://localhost:8080/warname/base/path"));
        assertThat(interfaceName, is("BasePathBcdResource"));
    }
}