package uk.gov.justice.services.eventsourcing.source.api.security;

import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class AccessController {

    @Inject
    private SystemUserProvider systemUserProvider;

    public void checkAccessControl(final HttpHeaders headers) {
        final String userId = headers.getRequestHeaders().getFirst(USER_ID);
        final String systemUserId = systemUserProvider.getContextSystemUserId().orElseThrow(() -> accessControlViolationException()).toString();
        if (!(userId!=null && userId.equals(systemUserId))) {
            throw accessControlViolationException();
        }
    }

    private AccessControlViolationException accessControlViolationException() {
        return new AccessControlViolationException("Requesting user is not a system user");
    }
}
