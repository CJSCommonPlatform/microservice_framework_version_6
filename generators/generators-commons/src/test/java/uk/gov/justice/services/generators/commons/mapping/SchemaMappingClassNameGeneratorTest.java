package uk.gov.justice.services.generators.commons.mapping;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaMappingClassNameGeneratorTest {

    @InjectMocks
    private SchemaMappingClassNameGenerator schemaMappingClassNameGenerator;

    @Test
    public void shouldCreateTheSchemaMappingClassNameFromTheContextNameAndItsInterfaceName() throws Exception {

        final String baseUri = "http://localhost:8080/test-command-api/command/api/rest/test";

        final String mappingClassName = schemaMappingClassNameGenerator.createMappingClassNameFrom(baseUri);

        assertThat(mappingClassName, is("TestCommandApiMediaTypeToSchemaIdMapper"));
    }

    @Test
    public void shouldFailIfTheBaseUriCannotBeConvertedToAUri() throws Exception {

        try {
            schemaMappingClassNameGenerator.createMappingClassNameFrom("not a uri");
            fail();
        } catch (final RamlBaseUriSyntaxException expected) {
            assertThat(expected.getMessage(), is("Failed to convert base uri from raml 'not a uri' into a URI"));
            assertThat(expected.getCause(), is(instanceOf(URISyntaxException.class)));
        }
    }
}
