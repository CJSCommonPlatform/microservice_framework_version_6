package uk.gov.justice.services.generators.commons.helper;


import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.commons.helper.Names.applicationNameFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.baseUriPathWithoutContext;
import static uk.gov.justice.services.generators.commons.helper.Names.buildResourceMethodNameWithNoMimeType;
import static uk.gov.justice.services.generators.commons.helper.Names.camelCase;
import static uk.gov.justice.services.generators.commons.helper.Names.mapperClassNameOf;
import static uk.gov.justice.services.generators.commons.helper.Names.nameFrom;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;
import static uk.gov.justice.services.generators.commons.helper.Names.resourceInterfaceNameOf;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.raml.core.GeneratorConfig;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

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
    public void shouldBuildResourceMethodName() throws Exception {
        Action action = httpAction().withHttpActionType(POST).build();
        action.setResource(resource().withRelativeUri("test").build());
        String shortMimeType = Names.buildResourceMethodName(action, new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("postCommandCreateUserTest"));
    }

    @Test
    public void shouldBuildResourceMethodNameWithNoMimeType() throws Exception {
        Action action = httpAction().withHttpActionType(POST).build();
        action.setResource(resource().withRelativeUri("test").build());
        String shortMimeType = buildResourceMethodNameWithNoMimeType(action);
        assertThat(shortMimeType, is("postTest"));
    }

    @Test
    public void shouldReturnInterfaceName() throws Exception {
        Resource resource = resource().withDefaultAction().build();
        String interfaceName = resourceInterfaceNameOf(resource);
        assertThat(interfaceName, is("SomecontextControllerCommandResource"));
    }

    @Test
    public void shouldReturnApplicationName() throws Exception {
        Raml raml = restRamlWithDefaults()
                .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                .build();
        String applicationName = applicationNameFrom(raml);
        assertThat(applicationName, is("CommandApiRestServiceApplication"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForMalformedUri() throws Exception {
        Raml raml = restRamlWithDefaults().withBaseUri("blah").build();
        applicationNameFrom(raml);
    }

    @Test
    public void shouldReturnPathIfNoContextFound() throws Exception {
        Raml raml = restRamlWithDefaults().withBaseUri("http://localhost:8080/webcontext").build();
        String applicationName = applicationNameFrom(raml);
        assertThat(applicationName, is("WebcontextApplication"));
    }

    @Test
    public void shouldRemoveContextFromBaseUri() throws Exception {
        Raml raml = restRamlWithDefaults()
                .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                .build();
        String applicationName = baseUriPathWithoutContext(raml);
        assertThat(applicationName, is("/command/api/rest/service"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForMalformedBaseUri() throws Exception {
        Raml raml = restRamlWithDefaults().withBaseUri("blah").build();
        baseUriPathWithoutContext(raml);
    }

    @Test
    public void shouldThrowExceptionForBaseUriThatDoesNotHaveEnoughPathElements() throws Exception {
        Raml raml = restRamlWithDefaults().withBaseUri("http://localhost:8080/webcontext").build();
        String applicationName = baseUriPathWithoutContext(raml);
        assertThat(applicationName, is("/webcontext"));
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
    public void shouldReturnTheClassNameForAResource() throws Exception {
        Resource resource = resource().withRelativeUri("/context.command").build();
        assertThat(mapperClassNameOf(resource), is("DefaultContextCommandResourceActionMapper"));
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

}
