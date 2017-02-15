package uk.gov.justice.services.raml.lintcheck.utils;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.raml.lintcheck.utils.RamlActionFinder.actionsFrom;
import static uk.gov.justice.services.raml.lintcheck.configuration.TestConfiguration.testConfig;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

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
        final Collection<String> actions = actionsFrom(testConfig().ramlGET());
        assertThat(actions, hasItems(
                is("test.firstcommand"),
                is("test.secondcommand")
        ));
    }

}