package uk.gov.justice.services.clients.rest.generator.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapters.test.utils.builder.MappingBuilder.defaultMapping;
import static uk.gov.justice.services.adapters.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;

import uk.gov.justice.raml.common.mapper.ActionMapping;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

public class ClientActionMappingGenerationTest {

    private static final String DESCRIPTION = mappingDescriptionWith(defaultMapping()).build();

    @Test
    public void shouldReturnListOfActionMappings() throws Exception {
        List<ActionMapping> actionMappings = new ClientActionMappingGeneration().listOfActionMappings(DESCRIPTION);

        assertThat(actionMappings.size(), is(1));

        ActionMapping actionMapping = actionMappings.get(0);
        assertThat(actionMapping.getName(), is("name1"));
        assertThat(actionMapping.getRequestType(), is("application/vnd.blah+json"));
    }

    @Test
    public void shouldReturnActionMapping() throws Exception {
        ClientActionMappingGeneration mappingGeneration = new ClientActionMappingGeneration();
        List<ActionMapping> actionMappings = mappingGeneration.listOfActionMappings(DESCRIPTION);
        Optional<ActionMapping> actionMapping = mappingGeneration.mappingOf(actionMappings, new MimeType("application/vnd.blah+json"), ActionType.POST);

        assertThat(actionMapping.isPresent(), is(true));
        assertThat(actionMapping.get().getName(), is("name1"));
        assertThat(actionMapping.get().getRequestType(), is("application/vnd.blah+json"));
    }

    @Test
    public void shouldReturnTheHandlesValueFromActionMappingName() throws Exception {
        ClientActionMappingGeneration mappingGeneration = new ClientActionMappingGeneration();
        List<ActionMapping> actionMappings = mappingGeneration.listOfActionMappings(DESCRIPTION);

        String value = mappingGeneration.handlesValue(Optional.of(actionMappings.get(0)), "header");
        assertThat(value, is("name1"));
    }

}