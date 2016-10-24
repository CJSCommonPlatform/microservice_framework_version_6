package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.test.utils.core.http.ResponseData;

import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Base class for matchers to match the data from <code>ResponseData</code>
 *
 * @param <R> extends ResponseData
 */
public abstract class ResponseMatcher<R extends ResponseData> extends TypeSafeDiagnosingMatcher<R> {
}
