package uk.gov.justice.raml.maven.lintchecker.rules.utils;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

import uk.gov.justice.raml.maven.lintchecker.rules.configuration.TestConfiguration;

import java.util.Collection;

import org.junit.Test;

public class RamlActionFinderTest {

    @Test
    public void shouldAssertWellDefined() {
        assertUtilityClassWellDefined(RamlActionFinder.class);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldFindActionsFromRamls() {
        final Collection<String> actions = RamlActionFinder.actionsFrom(TestConfiguration.testConfig().ramlGET());
        assertThat(actions, hasItems(
                is("test.firstcommand"),
                is("test.secondcommand")
        ));
    }

}