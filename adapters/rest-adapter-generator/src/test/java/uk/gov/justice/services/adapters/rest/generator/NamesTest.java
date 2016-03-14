package uk.gov.justice.services.adapters.rest.generator;

import org.junit.Test;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NamesTest {
    
    @Test
    public void shouldConvertMimetypeToShortMimeTypeString() throws Exception {
        String shortMimeType = Names.getShortMimeType(new MimeType("application/vnd.people.commands.create-user+json"));
        assertThat(shortMimeType, is("vndPeopleCommandsCreateUserJson"));
    }
    
    @Test
    public void shouldBuildMimeTypeInfix() throws Exception {
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.people.commands.create-user+json"));
        assertThat(shortMimeType, is("VndPeopleCommandsCreateUserJson"));
    }
    
    @Test
    public void shouldBuildMethodResourceName() throws Exception {
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.people.commands.create-user+json"));
        assertThat(shortMimeType, is("VndPeopleCommandsCreateUserJson"));
    }
    
    @Test
    public void shouldBuildResourceMethodName() throws Exception {
        Resource resource = new Resource();
        resource.setParentUri("");
        resource.setRelativeUri("test");
        Action action = new Action();
        action.setResource(resource);
        String shortMimeType = Names.buildMimeTypeInfix(new MimeType("application/vnd.people.commands.create-user+json"));
        assertThat(shortMimeType, is("VndPeopleCommandsCreateUserJson"));
    }

}