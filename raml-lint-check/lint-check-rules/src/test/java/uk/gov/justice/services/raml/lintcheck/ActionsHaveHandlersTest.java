package uk.gov.justice.services.raml.lintcheck;

import static uk.gov.justice.services.raml.lintcheck.configuration.TestConfiguration.testConfig;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.raml.maven.lintchecker.LintCheckConfiguration;
import uk.gov.justice.raml.maven.lintchecker.LintCheckerException;
import uk.gov.justice.services.raml.lintcheck.ActionsHaveHandlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActionsHaveHandlersTest {

    @Mock
    private LintCheckConfiguration lintCheckConfiguration;

    @Test
    public void shouldMatchAllActionsToHandlers() throws LintCheckerException {

        final ActionsHaveHandlers actionsHaveHandlers = new ActionsHaveHandlers();
        setField(actionsHaveHandlers, "basePackage", testConfig().basePackage());
        actionsHaveHandlers.execute(testConfig().ramlGET(), lintCheckConfiguration);

    }

    @Test(expected = LintCheckerException.class)
    public void shouldThrowLintCheckerException() throws LintCheckerException {

        final ActionsHaveHandlers actionsHaveHandlers = new ActionsHaveHandlers();
        setField(actionsHaveHandlers, "basePackage", testConfig().basePackage());
        actionsHaveHandlers.execute(testConfig().ramlGETmissing(), lintCheckConfiguration);

    }

}