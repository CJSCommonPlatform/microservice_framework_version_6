package uk.gov.justice.services.adapters.rest.generator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMapper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypes;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

public class RestAdapterGenerator_ActionNameToMediaTypesMapperTest extends BaseRestAdapterGeneratorTest {

    private static final MediaType MEDIA_TYPE_1 = new MediaType("application/vnd.ctx.command.command1+json");

    @Test
    public void shouldGenerateMediaTypeToSchemaIdMapper() throws Exception {
        final String actionName = "contextA.someAction";

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName(actionName)
                                                .withResponseType(MEDIA_TYPE_1.toString()))
                                        .withResponseTypes(MEDIA_TYPE_1.toString())
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        final Class<?> mediaTypesMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "WarnameActionNameToMediaTypesMapper");
        final ActionNameToMediaTypesMapper instance = (ActionNameToMediaTypesMapper) mediaTypesMapperClass.newInstance();

        final Map<String, MediaTypes> actionNameToMediaTypesMap = instance.getActionNameToMediaTypesMap();

        assertThat(actionNameToMediaTypesMap.size(), is(1));
        assertThat(actionNameToMediaTypesMap.get(actionName).getResponseMediaType(), is(Optional.of(MEDIA_TYPE_1)));
        assertThat(actionNameToMediaTypesMap.get(actionName).getRequestMediaType(), is(Optional.empty()));
    }
}
