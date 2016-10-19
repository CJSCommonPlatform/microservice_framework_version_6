package uk.gov.justice.services.test.utils.core.matchers;

import javax.ws.rs.core.Response;

import org.hamcrest.TypeSafeDiagnosingMatcher;

public abstract class ResponseMatcher<R extends Response> extends TypeSafeDiagnosingMatcher<R> {
}
