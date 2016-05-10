package uk.gov.justice.services.clients.rest.generator;

import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.raml.model.ParamType;
import org.raml.model.parameter.QueryParameter;

public class AbstractClientGeneratorTest {

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected RestClientGenerator restClientGenerator;
    protected JavaCompilerUtil compiler;

    @Before
    public void before() {
        restClientGenerator = new RestClientGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    protected QueryParameter queryParameterOf(String name, boolean required) {
        QueryParameter queryParameter1 = new QueryParameter();
        queryParameter1.setDisplayName(name);
        queryParameter1.setType(ParamType.STRING);
        queryParameter1.setRequired(required);
        return queryParameter1;
    }
}
