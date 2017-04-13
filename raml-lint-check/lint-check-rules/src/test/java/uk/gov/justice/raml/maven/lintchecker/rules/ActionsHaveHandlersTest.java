package uk.gov.justice.raml.maven.lintchecker.rules;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

import uk.gov.justice.raml.maven.lintchecker.LintCheckConfiguration;
import uk.gov.justice.raml.maven.lintchecker.LintCheckerException;
import uk.gov.justice.raml.maven.lintchecker.rules.configuration.TestConfiguration;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActionsHaveHandlersTest {

    @Mock
    private LintCheckConfiguration lintCheckConfiguration;

    @Mock
    private MavenProject mavenProject;

    @Mock
    private Log log;

    @Before
    public void setUp() throws DependencyResolutionRequiredException {
        when(lintCheckConfiguration.getMavenProject()).thenReturn(mavenProject);
        when(lintCheckConfiguration.getLog()).thenReturn(log);
        when(mavenProject.getRuntimeClasspathElements()).thenReturn(asList(new String[]{""}));
    }

    @Test
    public void shouldMatchAllActionsToHandlers() throws LintCheckerException, DependencyResolutionRequiredException {
        final ActionsHaveHandlers actionsHaveHandlers = new ActionsHaveHandlers();
        actionsHaveHandlers.execute(TestConfiguration.testConfig().ramlGET(), lintCheckConfiguration);

    }

    @Test(expected = LintCheckerException.class)
    public void shouldThrowLintCheckerException() throws LintCheckerException {
        final ActionsHaveHandlers actionsHaveHandlers = new ActionsHaveHandlers();
        actionsHaveHandlers.execute(TestConfiguration.testConfig().ramlGETmissing(), lintCheckConfiguration);
    }

}