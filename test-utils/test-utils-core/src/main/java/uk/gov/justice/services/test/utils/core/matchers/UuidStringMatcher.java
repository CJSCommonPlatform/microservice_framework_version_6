package uk.gov.justice.services.test.utils.core.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class UuidStringMatcher extends TypeSafeMatcher<String> {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    @Override
    protected boolean matchesSafely(final String maybeUuid) {
        if (maybeUuid == null) {
            return false;
        }

        return maybeUuid.matches(UUID_REGEX);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a string matching the pattern of a UUID");
    }

    @Factory
    public static Matcher<String> isAUuid() {
        return new UuidStringMatcher();
    }

}
