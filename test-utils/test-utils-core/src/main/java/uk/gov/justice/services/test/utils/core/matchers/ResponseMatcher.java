package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.test.utils.core.http.ResponseData;

import org.hamcrest.TypeSafeDiagnosingMatcher;

public abstract class ResponseMatcher<R extends ResponseData> extends TypeSafeDiagnosingMatcher<R> {
}
