package uk.gov.justice.services.generators.commons.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMapper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypes;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ActionNameToMediaTypesGeneratorTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    private JavaCompilerUtil compiler;

    @Before
    public void before() {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapperForGivenRamlWithPost() throws Exception {
        final String actionName = "contextA.someAction";
        final String requestType = "application/vnd.ctx.query.somemediatype1+json";
        final String responseType = "application/vnd.ctx.query.somemediatype2+json";

        final String description = mappingDescriptionWith(
                mapping()
                        .withName(actionName)
                        .withRequestType(requestType)
                        .withResponseType(responseType))
                .build();

        new ActionNameToMediaTypesGenerator().generateActionNameToMediaTypes(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(POST)
                                        .withDescription(
                                                description)
                                        .withMediaTypeWithDefaultSchema(requestType)
                                        .withResponseTypes(responseType)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> mediaTypesMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "WarnameActionNameToMediaTypesMapper");
        final ActionNameToMediaTypesMapper instance = (ActionNameToMediaTypesMapper) mediaTypesMapperClass.newInstance();

        final Map<String, MediaTypes> actionNameToMediaTypesMap = instance.getActionNameToMediaTypesMap();

        assertThat(actionNameToMediaTypesMap.size(), is(1));
        assertThat(actionNameToMediaTypesMap.get(actionName).getRequestMediaType(), is(Optional.of(new MediaType(requestType))));
        assertThat(actionNameToMediaTypesMap.get(actionName).getResponseMediaType(), is(Optional.of(new MediaType(responseType))));
    }

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapperForGivenRamlWithGet() throws Exception {
        final String actionName_1 = "contextA.someAction";
        final String responseType_1 = "application/vnd.ctx.query.somemediatype1+json";
        final String actionName_2 = "contextA.someOtherAction";
        final String responseType_2 = "application/vnd.ctx.query.somemediatype2+json";

        new ActionNameToMediaTypesGenerator().generateActionNameToMediaTypes(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(GET)
                                        .withDescription(
                                                mappingDescriptionWith(
                                                        mapping()
                                                                .withName(actionName_1)
                                                                .withResponseType(responseType_1),
                                                        mapping()
                                                                .withName(actionName_2)
                                                                .withResponseType(responseType_2))
                                                        .build())
                                        .withResponseTypes(responseType_1, responseType_2)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> mediaTypesMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "WarnameActionNameToMediaTypesMapper");
        final ActionNameToMediaTypesMapper instance = (ActionNameToMediaTypesMapper) mediaTypesMapperClass.newInstance();

        final Map<String, MediaTypes> actionNameToMediaTypesMap = instance.getActionNameToMediaTypesMap();

        assertThat(actionNameToMediaTypesMap.size(), is(2));
        assertThat(actionNameToMediaTypesMap.get(actionName_1).getResponseMediaType(), is(Optional.of(new MediaType(responseType_1))));
        assertThat(actionNameToMediaTypesMap.get(actionName_1).getRequestMediaType(), is(Optional.empty()));
        assertThat(actionNameToMediaTypesMap.get(actionName_2).getResponseMediaType(), is(Optional.of(new MediaType(responseType_2))));
        assertThat(actionNameToMediaTypesMap.get(actionName_2).getRequestMediaType(), is(Optional.empty()));
    }
}