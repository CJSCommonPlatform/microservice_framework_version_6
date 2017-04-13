package uk.gov.justice.raml.maven.lintchecker.rules;

import static java.lang.String.format;
import static java.lang.String.join;

import uk.gov.justice.raml.maven.lintchecker.LintCheckConfiguration;
import uk.gov.justice.raml.maven.lintchecker.LintCheckRuleFailedException;
import uk.gov.justice.raml.maven.lintchecker.LintCheckerException;
import uk.gov.justice.raml.maven.lintchecker.rules.utils.HandlerScanner;
import uk.gov.justice.raml.maven.lintchecker.rules.utils.RamlActionFinder;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.raml.model.Raml;

public class ActionsHaveHandlers implements LintCheckRule {

    @Override
    public void execute(final Raml raml, final LintCheckConfiguration lintCheckConfiguration) throws LintCheckerException {

        final Log log = lintCheckConfiguration.getLog();
        log.info("Lintchecker Plugin: Started scanning for @Handles methods");

        final HandlerScanner scanner = new HandlerScanner();

        final Collection<String> actionsFromHandlers = scanner.getHandlesActions(lintCheckConfiguration.getMavenProject());
        final Collection<String> actionsFromRaml = RamlActionFinder.actionsFrom(raml);

        log.debug(String.format("Lintchecker Plugin: Handlers found: %s", String.join(", ", actionsFromHandlers)));
        log.debug(String.format("Lintchecker Plugin: Actions in raml found: %s", String.join(", ", actionsFromRaml)));

        matchActionsHandlers(actionsFromHandlers, actionsFromRaml);
    }

    private void matchActionsHandlers(final Collection<String> actionsFromHandlers, final Collection<String> actionsFromRaml) throws LintCheckRuleFailedException {
        final Collection<String> unmatchedHandlers = new ArrayList<>(actionsFromHandlers);
        unmatchedHandlers.removeAll(actionsFromRaml);

        final Collection<String> unmatchedActions = new ArrayList<>(actionsFromRaml);
        unmatchedActions.removeAll(actionsFromHandlers);

        if(!unmatchedActions.isEmpty() || !unmatchedHandlers.isEmpty()) {
            throw new LintCheckRuleFailedException(exceptionMessage(unmatchedActions, unmatchedHandlers));
        }
    }

    private String exceptionMessage(final Collection<String> actions, final Collection<String> handlers) {

        final String message = "Actions have handlers lint check rule failure, " +
                "the following actions in raml have no valid handlers: %s and the " +
                "following handlers have been found without matching actions in raml: %s";

        return format(message, join(", ", actions), join(", ", handlers));
    }
}
