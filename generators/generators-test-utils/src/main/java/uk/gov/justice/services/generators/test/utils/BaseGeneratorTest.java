package uk.gov.justice.services.generators.test.utils;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class BaseGeneratorTest {
    protected static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected JavaCompilerUtil compiler;
    protected Generator generator;

    @Before
    public void before() {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

}
