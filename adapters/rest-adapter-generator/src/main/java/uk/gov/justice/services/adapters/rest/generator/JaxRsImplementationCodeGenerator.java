package uk.gov.justice.services.adapters.rest.generator;

import com.google.common.collect.ImmutableMap;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JMod.PUBLIC;
import static java.lang.String.format;

/**
 * Internal code generation class for generating JAX-RS resource implementation classes.
 */
class JaxRsImplementationCodeGenerator {

    private static final String ASYNC_DISPATCHER_BEAN_NAME = "asyncDispatcher";
    private static final String SYNC_DISPATCHER_BEAN_NAME = "syncDispatcher";
    private static final String HEADERS_CONTEXT = "headers";
    private static final String PARAMS_ARGUMENT_NAME = "params";
    private static final String REST_PROCESSOR_BEAN_NAME = "restProcessor";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private final JCodeModel codeModel;

    JaxRsImplementationCodeGenerator(final JCodeModel codeModel) {
        this.codeModel = codeModel;
    }

    JDefinedClass createImplementation(final JDefinedClass resourceInterface, final Component component) {
        final JPackage pkg = resourceInterface.getPackage();
        try {
            final JDefinedClass resourceImplementation = pkg._class("Default" + resourceInterface.name());
            resourceImplementation._implements(resourceInterface);
            addAnnotations(resourceImplementation, component);
            addImplementationMethods(resourceImplementation, resourceInterface);

            if (containsResourcesOfType(resourceInterface, POST)) {
                addAnnotatedProperty(resourceImplementation, AsynchronousDispatcher.class, ASYNC_DISPATCHER_BEAN_NAME, Inject.class);
            }
            if (containsResourcesOfType(resourceInterface, GET)) {
                addAnnotatedProperty(resourceImplementation, SynchronousDispatcher.class, SYNC_DISPATCHER_BEAN_NAME, Inject.class);
            }
            addAnnotatedProperty(resourceImplementation, RestProcessor.class, REST_PROCESSOR_BEAN_NAME, Inject.class);
            addAnnotatedProperty(resourceImplementation, HttpHeaders.class, HEADERS_CONTEXT, javax.ws.rs.core.Context.class);
            return resourceImplementation;

        } catch (JClassAlreadyExistsException ex) {
            throw new IllegalStateException("Class already exists", ex);
        }

    }

    private boolean containsResourcesOfType(final JDefinedClass resourceInterface, final String methodType) {
        return resourceInterface.methods().stream().filter((interfaceMethod) -> isResourceOfType(interfaceMethod, methodType)).findAny().isPresent();
    }

    private void addAnnotations(final JDefinedClass resourceImplementation, final Component component) {
        resourceImplementation.annotate(Stateless.class);
        final JAnnotationUse adapterAnnotation = resourceImplementation.annotate(Adapter.class);

        adapterAnnotation.param("value", component);
    }

    private void addImplementationMethods(final JDefinedClass resourceImplementation, final JDefinedClass resourceInterface) {
        final JClass str = codeModel.ref("String");
        resourceInterface.methods().forEach(interfaceMethod -> {
            final JMethod implementationMethod = resourceImplementation.method(PUBLIC, interfaceMethod.type(),
                    interfaceMethod.name());
            implementationMethod.annotate(Override.class);
            addMethodParams(implementationMethod, interfaceMethod);
            final JBlock body = implementationMethod.body();
            final JType map = codeModel.ref(Map.class.getCanonicalName()).narrow(str, str);
            final JClass mapBuilderClass = codeModel.ref(ImmutableMap.Builder.class.getName()).narrow(str, str);

            if (isResourceOfType(interfaceMethod, GET)) {
                body.decl(map, PARAMS_ARGUMENT_NAME, paramsMapBuilderInvocation(mapBuilderClass,
                        implementationMethod, false));
                body.directStatement(format("return %s.processSynchronously(%s::dispatch, %s, %s);",
                        REST_PROCESSOR_BEAN_NAME, SYNC_DISPATCHER_BEAN_NAME, HEADERS_CONTEXT, PARAMS_ARGUMENT_NAME));
            } else {
                body.decl(map, PARAMS_ARGUMENT_NAME, paramsMapBuilderInvocation(mapBuilderClass,
                        implementationMethod, true));
                body.directStatement(format("return %s.processAsynchronously(%s::dispatch, entity, %s, %s);",
                        REST_PROCESSOR_BEAN_NAME, ASYNC_DISPATCHER_BEAN_NAME, HEADERS_CONTEXT, PARAMS_ARGUMENT_NAME));
            }
        });
    }

    private boolean isResourceOfType(final JMethod interfaceMethod, final String methodType) {
        return interfaceMethod.annotations().stream()
                .filter(a -> a.getAnnotationClass().name().contains(methodType)).findAny().isPresent();
    }

    private void addAnnotatedProperty(final JDefinedClass resourceClass, final Class<?> clazz, final String name,
                                      final Class<? extends Annotation> annotation) {
        final JFieldVar dispatcherField = resourceClass.field(0, clazz, name);
        dispatcherField.annotate(annotation);
    }

    private void addMethodParams(final JMethod implMethod, final JMethod m) {
        m.params().forEach(p -> implMethod.param(p.type(), p.name()));
    }

    private JInvocation paramsMapBuilderInvocation(final JClass mapBuilderClass,
                                                   final JMethod implMethod,
                                                   final boolean skipLastParam) {
        JInvocation builderInstance = JExpr._new(mapBuilderClass);
        final List<JVar> params = implMethod.params();
        final int paramsSize = params.size() - (skipLastParam ? 1 : 0);
        for (int i = 0; i < paramsSize; i++) {
            final JVar p = params.get(i);
            builderInstance = builderInstance.invoke("put").arg(JExpr.lit(p.name())).arg(p);
        }
        return builderInstance.invoke("build");
    }

}
