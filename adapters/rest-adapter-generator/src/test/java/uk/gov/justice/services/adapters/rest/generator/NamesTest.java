package uk.gov.justice.services.adapters.rest.generator;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.rest.generator.Names.applicationNameFrom;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class NamesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Names.class);
    }

    @Test
    public void shouldConvertMimetypeToShortMimeTypeString() throws Exception {
        String shortMimeType = Names.getShortMimeType(new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("vndCommandCreateUserJson"));
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
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("VndCommandCreateUserJson"));
    }

    @Test
    public void shouldBuildMethodResourceName() throws Exception {
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("VndCommandCreateUserJson"));
    }

    @Test
    public void shouldBuildResourceMethodName() throws Exception {
        Action action = action().withActionType(POST).build();
        action.setResource(resource().withRelativeUri("test").build());
        String shortMimeType = Names.buildResourceMethodName(action, new MimeType("application/vnd.command.create-user+json"));
        assertThat(shortMimeType, is("postVndCommandCreateUserJsonTest"));
    }

    @Test
    public void shouldBuildVariableName() throws Exception {
        String variableName = Names.buildVariableName("somecontext.controller.command");
        assertThat(variableName, is("somecontextControllerCommand"));
    }

    @Test
    public void shouldReturnInterfaceName() throws Exception {
        Resource resource = resource().withDefaultAction().build();
        String interfaceName = Names.resourceInterfaceNameOf(resource);
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
        String applicationName = Names.baseUriPathWithoutContext(raml);
        assertThat(applicationName, is("/command/api/rest/service"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForMalformedBaseUri() throws Exception {
        Raml raml = restRamlWithDefaults().withBaseUri("blah").build();
        Names.baseUriPathWithoutContext(raml);
    }

    @Test
    public void shouldThrowExceptionForBaseUriThatDoesNotHaveEnoughPathElements() throws Exception {
        Raml raml = restRamlWithDefaults().withBaseUri("http://localhost:8080/webcontext").build();
        String applicationName = Names.baseUriPathWithoutContext(raml);
        assertThat(applicationName, is("/webcontext"));
    }

}
