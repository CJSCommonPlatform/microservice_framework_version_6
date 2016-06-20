package uk.gov.justice.services.clients.rest.generator.strategy;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.defaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;

import uk.gov.justice.services.generators.commons.mapping.ActionMapping;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

public class ClientMediaTypeGenerationTest {

    private static final String DESCRIPTION = mappingDescriptionWith(defaultMapping()).build();

    @Test
    public void shouldReturnEmptyListOfActionMappings() throws Exception {
        List<ActionMapping> actionMappings = new ClientMediaTypeGeneration().listOfActionMappings(DESCRIPTION);
        assertThat(actionMappings, is(emptyList()));
    }

    @Test
    public void shouldReturnEmptyOptional() throws Exception {
        Optional<ActionMapping> actionMapping = new ClientMediaTypeGeneration().mappingOf(emptyList(), new MimeType("application/vnd.blah+json"), ActionType.POST);
        assertThat(actionMapping, is(Optional.empty()));
    }

    @Test
    public void shouldReturnTheHandlesValueOfHeader() throws Exception {
        String value = new ClientMediaTypeGeneration().handlesValue(null, "header");
        assertThat(value, is("header"));
    }

}