package uk.gov.justice.services.generators.commons.mapping;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_BOUNDARY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_SEPARATOR;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.NAME_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.REQUEST_TYPE_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.RESPONSE_TYPE_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.listOf;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

import java.util.List;

import org.junit.Test;

public class ActionMappingTest {

    @Test
    public void shouldCreateSingleMappingWithRequestTypeFromString() throws Exception {

        List<ActionMapping> mappings = listOf(mappingDescriptionWith(
                mapping()
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build());

        assertThat(mappings, hasSize(1));
        ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test
    public void shouldCreateSingleMappingWithResponseTypeFromString() throws Exception {

        List<ActionMapping> mappings = listOf(mappingDescriptionWith(
                mapping()
                        .withResponseType("application/vnd.bbbb+json")
                        .withName("actionBCD"))
                .build());

        assertThat(mappings, hasSize(1));
        ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getResponseType(), is("application/vnd.bbbb+json"));
        assertThat(mapping.mimeTypeFor(GET), is("application/vnd.bbbb+json"));
        assertThat(mapping.getName(), is("actionBCD"));
    }

    @Test
    public void shouldCreateMappingsCollections() throws Exception {

        List<ActionMapping> mappings = listOf(mappingDescriptionWith(
                mapping()
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"),
                mapping()
                        .withRequestType("application/vnd.bbbbb+json")
                        .withName("actionB"))
                .build());

        assertThat(mappings, hasSize(2));
        assertThat(mappings, hasItems(allOf(hasProperty("requestType", equalTo("application/vnd.aaaa+json")), hasProperty("name", equalTo("actionA"))),
                allOf(hasProperty("requestType", equalTo("application/vnd.bbbbb+json")), hasProperty("name", equalTo("actionB")))));
    }

    @Test
    public void shouldCreateMappingIfPrefixedByOtherText() throws Exception {

        List<ActionMapping> mappings = listOf("Pre description of action" +
                mappingDescriptionWith(
                        mapping()
                                .withRequestType("application/vnd.aaaa+json")
                                .withName("actionA"))
                        .build());

        assertThat(mappings, hasSize(1));
        ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test
    public void shouldCreateMappingIfSuffixedByOtherText() throws Exception {

        List<ActionMapping> mappings = listOf(mappingDescriptionWith(
                mapping()
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build() + "Post description of action");

        assertThat(mappings, hasSize(1));
        ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithNoMapping() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithNull() throws Exception {
        listOf(null);
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMultipleRequests() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithStartMappingBoundaryMissing() throws Exception {
        listOf(MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithEndMappingBoundaryMissing() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithExtraCharacterAfterName() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ".: actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMultipleResponses() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                RESPONSE_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                RESPONSE_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingRequestFieldSeparator() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + " application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingNameFieldSeparator() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + " actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingRequestOrResponse() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingName() throws Exception {
        listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                MAPPING_BOUNDARY + "\n");
    }

}
