package uk.gov.justice.services.adapters.rest.generator;

import org.junit.Test;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

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

}