package uk.gov.justice.services.raml.lintcheck;

import static java.lang.String.format;
import static java.lang.String.join;
import static uk.gov.justice.services.raml.lintcheck.utils.RamlActionFinder.actionsFrom;

import uk.gov.justice.raml.maven.lintchecker.LintCheckConfiguration;
import uk.gov.justice.raml.maven.lintchecker.LintCheckRule;
import uk.gov.justice.raml.maven.lintchecker.LintCheckerException;
import uk.gov.justice.services.raml.lintcheck.utils.HandlerScanner;

import java.util.ArrayList;
import java.util.Collection;

import org.raml.model.Raml;

public class ActionsHaveHandlers implements LintCheckRule{

    private String basePackage; //Initialised from maven config by maven

    @Override
    public void execute(final Raml raml, final LintCheckConfiguration lintCheckConfiguration) throws LintCheckerException {

        final HandlerScanner scanner = new HandlerScanner(basePackage);

        final Collection<String> actionsFromHandlers = scanner.getHandlesActions();
        final Collection<String> actionsFromRaml = actionsFrom(raml);

        final Collection<String> unmatchedHandlers = new ArrayList<>(actionsFromHandlers);
        unmatchedHandlers.removeAll(actionsFromRaml);

        final Collection<String> unmatchedActions = new ArrayList<>(actionsFromRaml);
        unmatchedActions.removeAll(actionsFromHandlers);

        if(!unmatchedActions.isEmpty() || !unmatchedHandlers.isEmpty()) {
            throw new LintCheckerException(exceptionMessage(unmatchedActions, unmatchedHandlers));
        }
    }

    private String exceptionMessage(final Collection<String> actions, final Collection<String> handlers) {

        final String message = "Actions have handlers lint check rule failure, " +
                "the following actions in raml have no valid handlers: %s and the " +
                "following handlers have been found without matching actions in raml: %s";

        return format(message, join(", ", actions), join(", ", handlers));
    }
}
