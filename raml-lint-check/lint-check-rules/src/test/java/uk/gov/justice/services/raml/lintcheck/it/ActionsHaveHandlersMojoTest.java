package uk.gov.justice.services.raml.lintcheck.it;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.raml.maven.lintchecker.LintCheckMojo;
import uk.gov.justice.raml.maven.test.utils.BetterAbstractMojoTestCase;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

public class ActionsHaveHandlersMojoTest extends BetterAbstractMojoTestCase {

    public void testShouldFailTest() throws Exception {

        final File pom = getTestFile( "src/test/resources/it/pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        final LintCheckMojo myMojo = (LintCheckMojo) lookupConfiguredMojo(pom, "lint-check");

        assertNotNull( myMojo );

        try {
            myMojo.execute();
            fail();
        } catch (MojoExecutionException e) {
            assertThat(e.getCause().toString(), containsString("without matching actions in raml: test.secondcommand"));
        }
    }
}
