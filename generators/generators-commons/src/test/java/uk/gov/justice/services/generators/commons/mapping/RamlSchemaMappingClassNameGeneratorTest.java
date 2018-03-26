package uk.gov.justice.services.generators.commons.mapping;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RamlSchemaMappingClassNameGeneratorTest {

    @InjectMocks
    private RamlSchemaMappingClassNameGenerator ramlSchemaMappingClassNameGenerator;

    @Test
    public void shouldCreateTheSchemaMappingClassNameFromTheContextNameAndItsInterfaceName() throws Exception {

        final String baseUri = "http://localhost:8080/people-command-api/command/api/rest/people";

        final String mappingClassName = ramlSchemaMappingClassNameGenerator.createMappingClassNameFrom(baseUri, MediaTypeToSchemaIdMapper.class);

        assertThat(mappingClassName, is("PeopleCommandApiMediaTypeToSchemaIdMapper"));
    }

    @Test
    public void shouldFailIfTheBaseUriCannotBeConvertedToAUri() throws Exception {

        try {
            ramlSchemaMappingClassNameGenerator.createMappingClassNameFrom("not a uri", MediaTypeToSchemaIdMapper.class);
            fail();
        } catch (final RamlBaseUriSyntaxException expected) {
            assertThat(expected.getMessage(), is("Failed to convert base uri from raml 'not a uri' into a URI"));
            assertThat(expected.getCause(), is(instanceOf(URISyntaxException.class)));
        }
    }
}
