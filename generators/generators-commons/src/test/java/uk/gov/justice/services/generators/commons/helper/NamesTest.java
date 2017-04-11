package uk.gov.justice.services.generators.commons.helper;


import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.commons.helper.Names.buildResourceMethodNameWithNoMimeType;
import static uk.gov.justice.services.generators.commons.helper.Names.camelCase;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingDescriptionBuilder.mappingDescriptionWith;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition;
import uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder;
import uk.gov.justice.services.generators.test.utils.builder.MappingBuilder;

import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Action;
import org.raml.model.MimeType;

@RunWith(MockitoJUnitRunner.class)
public class NamesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private GeneratorConfig generatorConfig;

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Names.class);
    }

    @Test
    public void shouldBuildResourceMethodNameFromActionAndMimeType() throws Exception {
        final MappingBuilder mappingBuilder = mapping().withName("command.create-user").withRequestType("application/vnd.command.create-user+json");
        final Action action = httpAction().withHttpActionType(POST).withDescription(mappingDescriptionWith(mappingBuilder).build()).build();

        action.setResource(resource().withRelativeUri("test").build());
        final String shortMimeType = Names.buildResourceMethodName(action, new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("postCommandCreateUserTest"));
    }

    @Test
    public void shouldBuildResourceMethodNameFromActionIfNoMapping() throws Exception {
        final Action action = httpAction().withHttpActionType(POST).build();

        action.setResource(resource().withRelativeUri("test").build());
        final String shortMimeType = Names.buildResourceMethodName(action, new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("postTest"));
    }

    @Test
    public void shouldBuildResourceMethodNameFromActionAndNullMimeType() throws Exception {
        final MappingBuilder mappingBuilder = mapping().withName("command.create-user").withRequestType("application/vnd.command.create-user+json");
        final Action action = httpAction().withHttpActionType(POST).withDescription(mappingDescriptionWith(mappingBuilder).build()).build();

        action.setResource(resource().withRelativeUri("test").build());
        final String shortMimeType = Names.buildResourceMethodName(action, null);
        assertThat(shortMimeType, is("postTest"));
    }

    @Test
    public void shouldBuildResourceMethodNameFromAction() throws Exception {
        final MappingBuilder mappingBuilder = mapping().withName("command.create-user").withRequestType("application/vnd.command.create-user+json");
        final Action action = httpAction().withHttpActionType(GET).withDescription(mappingDescriptionWith(mappingBuilder).build()).build();

        action.setResource(resource().withRelativeUri("test").build());
        final String shortMimeType = Names.buildResourceMethodNameWithNoMimeType(action);
        assertThat(shortMimeType, is("getTest"));
    }

    @Test
    public void shouldBuildResourceMethodNameWithNoMimeType() throws Exception {
        final Action action = httpAction().withHttpActionType(POST).build();
        action.setResource(resource().withRelativeUri("test").build());
        final String shortMimeType = buildResourceMethodNameWithNoMimeType(action);
        assertThat(shortMimeType, is("postTest"));
    }

    @Test
    public void shouldReturnHeaderOfStringMimeType() throws Exception {
        // Removes the application/vnd
        final String section = "application/vnd.ctx.command.get-user+json".substring("application/vnd.ctx.command.get-user+json".indexOf('.') + 1);
        assertThat(section.substring(0, section.indexOf('+')), is("ctx.command.get-user"));
    }

    @Test
    public void shouldReturnHeaderOfMimeType() throws Exception {
        assertThat(nameFrom(new MimeType("application/vnd.ctx.command.get-user+json")), is("ctx.command.get-user"));
    }

    @Test
    public void shouldReturnPackageNameWithBasePackageNameOnly() throws Exception {
        when(generatorConfig.getBasePackageName()).thenReturn("base.package");
        assertThat(packageNameOf(generatorConfig, ""), is("base.package"));
    }

    @Test
    public void shouldReturnPackageNameWithBasePackageNameAndSubPackage() throws Exception {
        when(generatorConfig.getBasePackageName()).thenReturn("base.package");
        assertThat(packageNameOf(generatorConfig, "sub.package"), is("base.package.sub.package"));
    }

    @Test
    public void shouldCamelCaseStringAfterFirstCharacterAtEachDotCharacter() throws Exception {
        assertThat(camelCase("this.is.a.test"), is("thisIsATest"));
    }

    @Test
    public void shouldConstructDelimitedStringOfMimeTypes() throws Exception {
        final String expectedResult = "command.create-user,command.update-user,command.delete-user";

        final Stream<MimeType> mimeTypeStream = Stream.of(
                new MimeType("application/vnd.command.create-user+json"),
                new MimeType("application/vnd.command.update-user+json"),
                new MimeType("application/vnd.command.delete-user+json"));

        assertThat(Names.namesListStringFrom(mimeTypeStream, ","), is(expectedResult));
    }

    @Test
    public void shouldGenerateCorrectResourceNameForPostWithRequestMimeType() {
        final Action action = HttpActionBuilder.defaultPostAction().withResourceUri("/some/path/{recipeId}/").build();
        final ActionMimeTypeDefinition actionMimeTypeDefinition = ActionMimeTypeDefinition.definitionWithRequest(new MimeType("application/vnd.command.create-user+json"));

        assertThat(Names.buildResourceMethodNameFromVerbUriAndMimeType(action, actionMimeTypeDefinition), equalTo("postSomePathRecipeIdCommandCreateUser"));
    }

    @Test
    public void shouldGenerateCorrectResourceNameForGetWithResponseMimeType() {
        final Action action = HttpActionBuilder.defaultGetAction().withResourceUri("/some/path/{recipeId}/").build();
        final ActionMimeTypeDefinition actionMimeTypeDefinition = ActionMimeTypeDefinition.definitionWithResponse(new MimeType("application/vnd.command.create-user+json"));

        assertThat(Names.buildResourceMethodNameFromVerbUriAndMimeType(action, actionMimeTypeDefinition), equalTo("getSomePathRecipeIdCommandCreateUser"));
    }
}

