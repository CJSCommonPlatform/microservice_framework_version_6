package uk.gov.justice.services.generators.commons.mapping;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_BOUNDARY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_SEPARATOR;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.NAME_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.REQUEST_TYPE_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.RESPONSE_TYPE_KEY;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

import java.util.List;

import org.junit.Test;
import org.raml.model.MimeType;

public class ActionMappingParserTest {

    private final ActionMappingParser actionMappingParser = new ActionMappingParser();

    @Test
    public void shouldCreateSingleMappingWithRequestTypeFromString() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build());

        assertThat(mappings, hasSize(1));
        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test
    public void shouldCreateSingleMappingWithResponseTypeFromString() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withResponseType("application/vnd.bbbb+json")
                        .withName("actionBCD"))
                .build());

        assertThat(mappings, hasSize(1));
        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getResponseType(), is("application/vnd.bbbb+json"));
        assertThat(mapping.mimeTypeFor(GET), is("application/vnd.bbbb+json"));
        assertThat(mapping.getName(), is("actionBCD"));
    }


    @Test
    public void shouldReturnMappingFromRamlAndMimeType() throws Exception {

        final ActionMapping actionMapping = actionMappingParser.valueOf(httpAction(GET)
                        .withResponseTypes(
                                "application/vnd.ctx.query2+json",
                                "application/vnd.ctx.query1+json")
                        .with(mapping()
                                .withName("actionABC")
                                .withResponseType("application/vnd.ctx.query1+json"))
                        .with(mapping()
                                .withName("actionBCD")
                                .withResponseType("application/vnd.ctx.query2+json")).build()
                , new MimeType("application/vnd.ctx.query1+json"));

        assertThat(actionMapping, not(nullValue()));
        assertThat(actionMapping.getName(), is("actionABC"));
        assertThat(actionMapping.getResponseType(), is("application/vnd.ctx.query1+json"));

    }

    @Test
    public void shouldReturnResponseTypeForGetAction() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withResponseType("application/vnd.bbbb+json")
                        .withName("actionBCD"))
                .build());

        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.mimeTypeFor(GET), is("application/vnd.bbbb+json"));
    }

    @Test
    public void shouldReturnRequestTypeForDeletePatchPostAndPutAction() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build());

        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.mimeTypeFor(DELETE), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(PATCH), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(PUT), is("application/vnd.aaaa+json"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForHeadAction() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withResponseType("application/vnd.bbbb+json")
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build());

        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.mimeTypeFor(HEAD), is("application/vnd.aaaa+json"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForTraceAction() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withResponseType("application/vnd.bbbb+json")
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build());

        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.mimeTypeFor(TRACE), is("application/vnd.aaaa+json"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForOptionsAction() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withResponseType("application/vnd.bbbb+json")
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build());

        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.mimeTypeFor(OPTIONS), is("application/vnd.aaaa+json"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCreateMappingsCollections() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
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

        final List<ActionMapping> mappings = actionMappingParser.listOf("Pre description of action" +
                mappingDescriptionWith(
                        mapping()
                                .withRequestType("application/vnd.aaaa+json")
                                .withName("actionA"))
                        .build());

        assertThat(mappings, hasSize(1));
        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test
    public void shouldCreateMappingIfSuffixedByOtherText() throws Exception {

        final List<ActionMapping> mappings = actionMappingParser.listOf(mappingDescriptionWith(
                mapping()
                        .withRequestType("application/vnd.aaaa+json")
                        .withName("actionA"))
                .build() + "Post description of action");

        assertThat(mappings, hasSize(1));
        final ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("application/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("application/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithNoMapping() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithNull() throws Exception {
        actionMappingParser.listOf(null);
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMultipleRequests() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithStartMappingBoundaryMissing() throws Exception {
        actionMappingParser.listOf(MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithEndMappingBoundaryMissing() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithExtraCharacterAfterName() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ".: actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMultipleResponses() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                RESPONSE_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                RESPONSE_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingRequestFieldSeparator() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + " application/vnd.aaaa+json\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingNameFieldSeparator() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                NAME_KEY + " actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingRequestOrResponse() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                NAME_KEY + ": actionA\n" +
                MAPPING_BOUNDARY + "\n");
    }

    @Test(expected = RamlValidationException.class)
    public void shouldFailWithMissingName() throws Exception {
        actionMappingParser.listOf(MAPPING_BOUNDARY + "\n" +
                MAPPING_SEPARATOR + "\n" +
                REQUEST_TYPE_KEY + ": application/vnd.aaaa+json\n" +
                MAPPING_BOUNDARY + "\n");
    }

}
