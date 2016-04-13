package uk.gov.justice.services.adapters.rest.generator;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder;
import uk.gov.justice.services.core.annotation.Component;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class NamesTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Names.class);
    }

    @Test
    public void shouldConvertMimetypeToShortMimeTypeString() throws Exception {
        String shortMimeType = Names.getShortMimeType(new MimeType("application/vnd.commands.create-user+json"));
        assertThat(shortMimeType, is("vndCommandsCreateUserJson"));
    }

    @Test
    public void shouldReturnEmptyIfMimeTypeIsNull() throws Exception {
        String shortMimeType = Names.getShortMimeType(null);
        assertThat(shortMimeType, is(""));
    }

    @Test
    public void shouldHandleOtherMimeTypes() throws Exception {
        String shortMimeType = Names.getShortMimeType(new MimeType("application/x-www-test-type+json"));
        assertThat(shortMimeType, is("testtypejson"));
    }

    @Test
    public void shouldBuildMimeTypeInfix() throws Exception {
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.commands.create-user+json"));
        assertThat(shortMimeType, is("VndCommandsCreateUserJson"));
    }

    @Test
    public void shouldBuildMethodResourceName() throws Exception {
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.commands.create-user+json"));
        assertThat(shortMimeType, is("VndCommandsCreateUserJson"));
    }

    @Test
    public void shouldBuildResourceMethodName() throws Exception {
        Action action = action().with(ActionType.POST).build();
        action.setResource(resource().withRelativeUri("test").build());
        String shortMimeType = Names.buildResourceMethodName(action, new MimeType("application/vnd.commands.create-user+json"));
        assertThat(shortMimeType, is("postVndCommandsCreateUserJsonTest"));
    }

    @Test
    public void shouldBuildVariableName() throws Exception {
        String variableName = Names.buildVariableName("somecontext.controller.commands");
        assertThat(variableName, is("somecontextControllerCommands"));
    }

    @Test
    public void shouldReturnInterfaceName() throws Exception {
        Resource resource = resource().withDefaultAction().build();
        String interfaceName = Names.resourceInterfaceNameOf(resource);
        assertThat(interfaceName, is("SomecontextControllerCommandsResource"));
    }

    @Test
    public void shouldReturnApplicationName() throws Exception {
        Raml raml = RamlBuilder.restRamlWithDefaults().build();
        String applicationName = Names.applicationNameOf(raml);
        assertThat(applicationName, is("CommandApiRestServiceApplication"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForMalformedUri() throws Exception {
        Raml raml = RamlBuilder.restRamlWithDefaults().withBaseUri("blah").build();
        Names.applicationNameOf(raml);
    }

    @Test
    public void shouldReturnPathIfNoContextFound() throws Exception {
        Raml raml = RamlBuilder.restRamlWithDefaults().withBaseUri("http://localhost:8080/webcontext").build();
        String applicationName = Names.applicationNameOf(raml);
        assertThat(applicationName, is("WebcontextApplication"));
    }

    @Test
    public void shouldRemoveContextFromBaseUri() throws Exception {
        Raml raml = RamlBuilder.restRamlWithDefaults().build();
        String applicationName = Names.baseUriPathWithoutContext(raml);
        assertThat(applicationName, is("/command/api/rest/service"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForMalformedBaseUri() throws Exception {
        Raml raml = RamlBuilder.restRamlWithDefaults().withBaseUri("blah").build();
        Names.baseUriPathWithoutContext(raml);
    }

    @Test
    public void shouldThrowExceptionForBaseUriThatDoesNotHaveEnoughPathElements() throws Exception {
        Raml raml = RamlBuilder.restRamlWithDefaults().withBaseUri("http://localhost:8080/webcontext").build();
        String applicationName = Names.baseUriPathWithoutContext(raml);
        assertThat(applicationName, is("/webcontext"));
    }

    @Test
    public void shouldReturnComponentFromBaseUriForCommandApi() throws Exception {
        Raml raml = RamlBuilder.restRamlWithCommandApiDefaults().build();
        Component component = Names.componentFromBaseUriIn(raml);
        assertThat(component, is(Component.COMMAND_API));
    }

    @Test
    public void shouldReturnComponentFromBaseUriForQueryApi() throws Exception {
        Raml raml = RamlBuilder.restRamlWithQueryApiDefaults().build();
        Component component = Names.componentFromBaseUriIn(raml);
        assertThat(component, is(Component.QUERY_API));
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfNoValidPillarAndTier() throws Exception {
        Raml raml = new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/event/listener/rest/service").build();

        exception.expect(IllegalStateException.class);
        exception.expectMessage("Base URI must contain valid pillar and tier: http://localhost:8080/warname/event/listener/rest/service");

        Names.componentFromBaseUriIn(raml);
    }
}
