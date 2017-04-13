package uk.gov.justice.raml.maven.lintchecker.rules.utils;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.raml.maven.lintchecker.LintCheckPluginException;

import java.util.Collection;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandlerScannerTest {

    @Mock
    private MavenProject mavenProject;

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldMatchValidActions() throws LintCheckPluginException, DependencyResolutionRequiredException {

        when(mavenProject.getRuntimeClasspathElements()).thenReturn(asList(new String[]{""}));

        final HandlerScanner handlerScanner =
                new HandlerScanner();

        final Collection<String> handlesActions = handlerScanner.getHandlesActions(mavenProject);

        assertThat(handlesActions.size(), is(2));
        assertThat(handlesActions, hasItems(is("test.firstcommand"), is("test.secondcommand")));
        assertThat(handlesActions, not(hasItem(is("test.thirdcommand"))));
    }
}