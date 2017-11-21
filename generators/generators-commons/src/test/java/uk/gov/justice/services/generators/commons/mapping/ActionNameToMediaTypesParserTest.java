package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Raml;

@RunWith(MockitoJUnitRunner.class)
public class ActionNameToMediaTypesParserTest {

    @Spy
    private final ActionMappingParser actionMappingParser = new ActionMappingParser();

    @InjectMocks
    private ActionNameToMediaTypesParser actionNameToMediaTypesParser;

    @Test
    public void shouldReturnActionNameMappingsForGetResource() throws Exception {

        final String name_1 = "contextA.someAction";
        final String responseType_1 = "application/vnd.ctx.query.somemediatype1+json";
        final String responseType_2 = "application/vnd.ctx.query.somemediatype2+json";

        final String name_3 = "contextA.someOtherAction";
        final String responseType_3 = "application/vnd.ctx.query.somemediatype3+json";

        final String mappingDescription = mappingDescriptionWith(
                mapping()
                        .withName(name_1)
                        .withResponseType(responseType_1),
                mapping()
                        .withName(name_1)
                        .withResponseType(responseType_2),
                mapping()
                        .withName(name_3)
                        .withResponseType(responseType_3))
                .build();

        final Raml raml = restRamlWithQueryApiDefaults()
                .with(resource("/user")
                        .with(httpActionWithDefaultMapping(GET)
                                .withDescription(mappingDescription)
                                .withResponseTypes(
                                        responseType_1,
                                        responseType_2,
                                        responseType_3)
                        )
                ).build();

        final List<ActionMapping> actionMappings = asList(
                new ActionMapping(name_1, null, responseType_1),
                new ActionMapping(name_1, null, responseType_2),
                new ActionMapping(name_3, null, responseType_3)
        );

        final List<ActionNameMapping> actionNameMappings = actionNameToMediaTypesParser.parseFrom(raml);

        assertThat(actionNameMappings.size(), is(3));
        assertThat(actionNameMappings, hasItems(
                new ActionNameMapping(name_1, null, new MediaType(responseType_1)),
                new ActionNameMapping(name_1, null, new MediaType(responseType_2)),
                new ActionNameMapping(name_3, null, new MediaType(responseType_3))
        ));
    }

    @Test
    public void shouldReturnActionNameMappingsForPostPutPatchAndDeleteResources() throws Exception {

        final String name_1 = "contextA.someAction-1";
        final String requestType_1 = "application/vnd.ctx.query.request-1+json";
        final String responseType_1 = "application/vnd.ctx.query.response-1+json";

        final String name_2 = "contextA.someAction-2";
        final String requestType_2 = "application/vnd.ctx.query.request-2+json";
        final String responseType_2 = "application/vnd.ctx.query.response-2+json";

        final String name_3 = "contextA.someAction-3";
        final String requestType_3 = "application/vnd.ctx.query.request-3+json";
        final String responseType_3 = "application/vnd.ctx.query.response-3+json";

        final String name_4 = "contextA.someAction-4";
        final String requestType_4 = "application/vnd.ctx.query.request-4+json";
        final String responseType_4 = "application/vnd.ctx.query.response-4+json";

        final String mappingDescription_1 = mappingDescriptionWith(
                mapping()
                        .withName(name_1)
                        .withRequestType(requestType_1)
                        .withResponseType(responseType_1))
                .build();

        final String mappingDescription_2 = mappingDescriptionWith(
                mapping()
                        .withName(name_2)
                        .withRequestType(requestType_2)
                        .withResponseType(responseType_2))
                .build();

        final String mappingDescription_3 = mappingDescriptionWith(
                mapping()
                        .withName(name_3)
                        .withRequestType(requestType_3)
                        .withResponseType(responseType_3))
                .build();

        final String mappingDescription_4 = mappingDescriptionWith(
                mapping()
                        .withName(name_4)
                        .withRequestType(requestType_4)
                        .withResponseType(responseType_4))
                .build();

        final Raml raml = restRamlWithQueryApiDefaults()
                .with(resource("/user")
                        .with(httpActionWithDefaultMapping(POST)
                                .withDescription(mappingDescription_1)
                                .withMediaTypeWithDefaultSchema(requestType_1)
                                .withResponseTypes(responseType_1)
                        )
                        .with(httpActionWithDefaultMapping(PUT)
                                .withDescription(mappingDescription_2)
                                .withMediaTypeWithDefaultSchema(requestType_2)
                                .withResponseTypes(responseType_3)
                        )
                        .with(httpActionWithDefaultMapping(PATCH)
                                .withDescription(mappingDescription_3)
                                .withMediaTypeWithDefaultSchema(requestType_3)
                                .withResponseTypes(responseType_4)
                        )
                        .with(httpActionWithDefaultMapping(DELETE)
                                .withDescription(mappingDescription_4)
                                .withMediaTypeWithDefaultSchema(requestType_4)
                                .withResponseTypes(responseType_4)
                        )

                ).build();

        final List<ActionNameMapping> actionNameMappings = actionNameToMediaTypesParser.parseFrom(raml);

        assertThat(actionNameMappings.size(), is(4));
        assertThat(actionNameMappings, hasItems(
                new ActionNameMapping(name_1, new MediaType(requestType_1), new MediaType(responseType_1)),
                new ActionNameMapping(name_2, new MediaType(requestType_2), new MediaType(responseType_2)),
                new ActionNameMapping(name_3, new MediaType(requestType_3), new MediaType(responseType_3)),
                new ActionNameMapping(name_4, new MediaType(requestType_4), new MediaType(responseType_4))
        ));
    }

    @Test
    public void shouldReturnActionNameMappingsForPostResourceSameActionMappedToTwoMediaTypes() throws Exception {

        final String name_1 = "contextC.someAction";
        final String requestType_1 = "application/vnd.ctx.command.somemediatype1+json";
        final String requestType_2 = "application/vnd.ctx.command.somemediatype2+json";

        final String mappingDescription = mappingDescriptionWith(
                mapping()
                        .withName(name_1)
                        .withRequestType(requestType_1),
                mapping()
                        .withName(name_1)
                        .withRequestType(requestType_2))
                .build();


        final Raml raml = restRamlWithCommandApiDefaults()
                .with(resource("/case")
                        .with(httpActionWithDefaultMapping(POST)
                                .withDescription(mappingDescription)
                                .withMediaTypeWithDefaultSchema(requestType_1)
                                .withMediaTypeWithDefaultSchema(requestType_2)
                        )

                ).build();

        final List<ActionNameMapping> actionNameMappings = actionNameToMediaTypesParser.parseFrom(raml);

        assertThat(actionNameMappings.size(), is(2));
        assertThat(actionNameMappings, hasItems(
                new ActionNameMapping(name_1, new MediaType(requestType_1), null),
                new ActionNameMapping(name_1, new MediaType(requestType_2), null)
        ));
    }
}
