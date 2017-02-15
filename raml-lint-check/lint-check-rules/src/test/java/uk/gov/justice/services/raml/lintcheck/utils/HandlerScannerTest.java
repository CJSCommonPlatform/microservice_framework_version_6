package uk.gov.justice.services.raml.lintcheck.utils;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.raml.lintcheck.configuration.TestConfiguration.testConfig;

import java.util.Collection;

import org.junit.Test;


public class HandlerScannerTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldMatchValidActions() {

        final HandlerScanner handlerScanner =
                new HandlerScanner(testConfig().basePackage());

        final Collection<String> handlesActions = handlerScanner.getHandlesActions();

        assertThat(handlesActions.size(), is(2));
        assertThat(handlesActions, hasItems(is("test.firstcommand"), is("test.secondcommand")));
        assertThat(handlesActions, not(hasItem(is("test.thirdcommand"))));
    }
}