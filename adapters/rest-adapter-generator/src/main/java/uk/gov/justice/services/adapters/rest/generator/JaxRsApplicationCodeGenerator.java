package uk.gov.justice.services.adapters.rest.generator;

import static com.sun.codemodel.JMod.PUBLIC;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static uk.gov.justice.services.adapters.rest.generator.Names.applicationNameOf;
import static uk.gov.justice.services.adapters.rest.generator.Names.baseUriPathWithoutContext;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.raml.model.Raml;

/**
 * Internal code generation class for generating the JAX-RS {@link Application} that ties the
 * resources to a base URI.
 */
class JaxRsApplicationCodeGenerator {

    private static final String DEFAULT_ANNOTATION_PARAMETER = "value";

    private final GeneratorConfig config;
    private final JCodeModel codeModel;

    /**
     * Constructor.
     *
     * @param codeModel the code model, which should already have the resource implementations and
     *                  interfaces created
     * @param config    the generator configuration
     */
    JaxRsApplicationCodeGenerator(final JCodeModel codeModel, final GeneratorConfig config) {
        this.codeModel = codeModel;
        this.config = config;
    }

    /**
     * Create an {@link Application} in the {@link JCodeModel}.
     *
     * @param raml                the RAML document being generated from
     * @param implementationNames a collection of fully qualified class names of the resource
     *                            implementation classes
     * @return the fully defined application class
     */
    JDefinedClass createApplication(final Raml raml, final Collection<String> implementationNames) {
        final JPackage pkg = codeModel._package(config.getBasePackageName());
        try {
            final JDefinedClass application = pkg._class(applicationNameOf(raml));
            addAnnotations(raml, application);
            addSuperClass(application);
            addMethods(application, implementationNames);

            return application;
        } catch (JClassAlreadyExistsException ex) {
            throw new IllegalStateException("Class already exists", ex);
        }

    }

    private void addAnnotations(final Raml raml, final JDefinedClass application) {
        application.annotate(ApplicationPath.class)
                .param(DEFAULT_ANNOTATION_PARAMETER, defaultIfBlank(baseUriPathWithoutContext(raml), "/"));
    }

    private void addSuperClass(final JDefinedClass application) {
        application._extends(Application.class);
    }

    private void addMethods(final JDefinedClass application, final Collection<String> implementationNames) {
        JType wildcardClassType = codeModel.ref(Class.class).narrow(codeModel.wildcard());
        JType classSetType = codeModel.ref(Set.class).narrow(wildcardClassType);

        JMethod getClassesMethod = application.method(PUBLIC, classSetType, "getClasses");

        getClassesMethod.annotate(Override.class);

        JBlock body = getClassesMethod.body();

        JVar classes = body.decl(classSetType, "classes");
        classes.init(JExpr._new(codeModel.ref(HashSet.class).narrow(wildcardClassType)));
        for (String className : implementationNames) {
            body.invoke(classes, "add").arg(codeModel._getClass(className).dotclass());
        }
        body._return(classes);
    }
}
