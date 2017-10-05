package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import java.util.Optional;

import org.junit.Test;

public class RestResourceBaseUriTest {

    @Test
    public void shouldReturnPathWithoutContext() throws Exception {
        assertThat(new RestResourceBaseUri("http://localhost:8080/warname/command/api/rest/service")
                .pathWithoutWebContext(), is("/command/api/rest/service"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForMalformedUri() throws Exception {
        new RestResourceBaseUri("blah").pathWithoutWebContext();
    }


    @Test
    public void shouldReturnComponent() {
        assertThat(new RestResourceBaseUri("http://localhost:8080/contextabc/command/api/rest/service")
                .component().get(), is(COMMAND_API));
        assertThat(new RestResourceBaseUri("http://localhost:8080/contextbcd/query/api/rest/service")
                .component().get(), is(QUERY_API));

    }

    @Test
    public void shouldReturnOptionalEmptyIfNoValidPillarAndTier() throws Exception {
        assertThat(new RestResourceBaseUri("http://localhost:8080/warname/event/listener/rest/service")
                .component(), is(Optional.empty()));
    }

    @Test
    public void shouldReturnClassNamePrefix() {
        assertThat(new RestResourceBaseUri("http://localhost:8080/contextabc/command/api/rest/service")
                .classNamePrefix(), is("CommandApi"));
        assertThat(new RestResourceBaseUri("http://localhost:8080/contextbcd/query/api/rest/service")
                .classNamePrefix(), is("QueryApi"));
        assertThat(new RestResourceBaseUri("http://localhost:8080/contextxyz/some/random-path")
                .classNamePrefix(), is("SomeRandomPath"));

    }

}
