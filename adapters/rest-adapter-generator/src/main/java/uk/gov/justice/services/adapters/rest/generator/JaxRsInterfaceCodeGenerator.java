package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.join;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.strip;
import static uk.gov.justice.services.adapters.rest.generator.Actions.responseMimeTypesOf;
import static uk.gov.justice.services.adapters.rest.generator.Names.GENERIC_PAYLOAD_ARGUMENT_NAME;
import static uk.gov.justice.services.adapters.rest.generator.Names.buildResourceMethodName;
import static uk.gov.justice.services.adapters.rest.generator.Names.buildVariableName;
import static uk.gov.justice.services.adapters.rest.generator.Names.resourceInterfaceNameOf;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.collect.ImmutableList;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;

class JaxRsInterfaceCodeGenerator {

    private static final List<Class<? extends Annotation>> JAXRS_HTTP_METHODS = ImmutableList.of(GET.class, POST.class);

    private static final String DEFAULT_ANNOTATION_PARAMETER = "value";
    private static final String REST_INTERFACE_PACKAGE_NAME = "resource";
    private final JCodeModel codeModel;
    private final GeneratorConfig configuration;
    private final Map<String, Set<String>> resourcesMethods;
    private final Map<String, Object> httpMethodAnnotations;

    JaxRsInterfaceCodeGenerator(final JCodeModel codeModel, final GeneratorConfig configuration) {
        this.codeModel = codeModel;
        this.configuration = configuration;
        this.resourcesMethods = new HashMap<>();
        this.httpMethodAnnotations = new HashMap<>();

        JAXRS_HTTP_METHODS.forEach(clazz -> httpMethodAnnotations.put(clazz.getSimpleName(), clazz));
    }

    JDefinedClass createInterface(final Resource resource) {

        String interfaceName = resourceInterfaceNameOf(resource);
        final JDefinedClass resourceInterface = interfaceClassOf(interfaceName);
        final String relativeUri = strip(resource.getRelativeUri(), "/");
        resourceInterface.annotate(Path.class).param(DEFAULT_ANNOTATION_PARAMETER, defaultIfBlank(relativeUri, "/"));
        addResourceMethods(resource, resourceInterface, relativeUri);

        return resourceInterface;
    }

    private JDefinedClass interfaceClassOf(final String name) {
        String actualName;
        int i = -1;
        while (true) {
            actualName = name + (++i == 0 ? "" : Integer.toString(i));
            if (!resourcesMethods.containsKey(actualName)) {
                resourcesMethods.put(actualName, new HashSet<>());
                break;
            }
        }

        final JPackage pkg = codeModel._package(join(".", configuration.getBasePackageName(), REST_INTERFACE_PACKAGE_NAME));
        try {
            return pkg._interface(actualName);
        } catch (JClassAlreadyExistsException ex) {
            throw new IllegalStateException("Class already exists", ex);
        }
    }

    private void addResourceMethods(final Resource resource,
                                    final JDefinedClass resourceInterface,
                                    final String resourceInterfacePath) {

        for (final Action action : resource.getActions().values()) {
            Collection<MimeType> responseMimeTypes = responseMimeTypesOf(action);

            if (!action.hasBody()) {
                addResourceMethod(resourceInterface, resourceInterfacePath, action, null, false, responseMimeTypes);
            } else if (action.getBody().size() == 1) {
                final MimeType bodyMimeType = action.getBody().values().iterator().next();
                addResourceMethod(resourceInterface, resourceInterfacePath, action, bodyMimeType, false, responseMimeTypes);
            } else {
                action.getBody().values().stream().sorted((t1, t2) -> t1.getType().compareTo(t2.getType()))
                        .forEach(bodyMimeType -> addResourceMethod(resourceInterface, resourceInterfacePath,
                                action, bodyMimeType, true, responseMimeTypes));
            }
        }
    }


    private JMethod addResourceMethod(final JDefinedClass resourceInterface,
                                      final String resourceInterfacePath,
                                      final Action action,
                                      final MimeType bodyMimeType,
                                      final boolean addBodyMimeTypeInMethodName, final Collection<MimeType> responseMimeTypes) {

        MimeType actualBodyMimeType = addBodyMimeTypeInMethodName ? bodyMimeType : null;

        String methodName = buildResourceMethodName(action, actualBodyMimeType);

        final JClass resourceMethodReturnType = codeModel.ref("javax.ws.rs.core.Response");

        final JMethod method = createResourceMethod(resourceInterface, methodName,
                resourceMethodReturnType);
        addHttpMethodAnnotation(action.getType().toString(), method);

        addConsumesAnnotation(bodyMimeType, method);
        addProducesAnnotation(responseMimeTypes, method);

        addPathParameters(action, method);
        addQueryParameters(action, method);
        addBodyParameters(bodyMimeType, method);
        return method;
    }



    private void addConsumesAnnotation(final MimeType bodyMimeType,
                                       final JMethod method) {
        if (bodyMimeType != null) {
            method.annotate(Consumes.class).param(DEFAULT_ANNOTATION_PARAMETER,
                    bodyMimeType.getType());
        }
    }

    private void addProducesAnnotation(
            final Collection<MimeType> responseMimeTypes,
            final JMethod method) {
        if (responseMimeTypes.isEmpty()) {
            return;
        }
        final JAnnotationArrayMember paramArray = method.annotate(
                Produces.class).paramArray(DEFAULT_ANNOTATION_PARAMETER);

        for (final MimeType responseMimeType : responseMimeTypes) {
            paramArray.param(responseMimeType.getType());
        }
    }

    private void addPathParameters(final Action action, final JMethod method) {
        addAllResourcePathParameters(action.getResource(), method);
    }

    private void addAllResourcePathParameters(Resource resource,
                                              final JMethod method) {
        for (final Entry<String, UriParameter> namedUriParameter : resource
                .getUriParameters().entrySet()) {
            addParameter(namedUriParameter.getKey(), PathParam.class, method);
        }
    }

    private void addParameter(final String name,
                              final Class<? extends Annotation> annotationClass,
                              final JMethod method) {

        final String argumentName = buildVariableName(name);

        final JVar argumentVariable = method
                .param(codeModel.ref("String"), argumentName);

        argumentVariable.annotate(annotationClass).param(
                DEFAULT_ANNOTATION_PARAMETER, name);

    }

    private void addQueryParameters(final Action action, final JMethod method) {
        action.getQueryParameters().keySet().stream().forEach(name ->
                addParameter(name, QueryParam.class, method)
        );
    }

    private void addBodyParameters(final MimeType bodyMimeType,
                                   final JMethod method) {
        if (bodyMimeType != null) {
            method.param(JsonObject.class, GENERIC_PAYLOAD_ARGUMENT_NAME);
        }
    }

    private JMethod createResourceMethod(final JDefinedClass resourceClass,
                                         final String methodName,
                                         final JClass returnType) {
        final Set<String> existingMethodNames = resourcesMethods.get(resourceClass.name());

        String actualMethodName;
        int i = -1;
        while (true) {
            actualMethodName = methodName + (++i == 0 ? "" : Integer.toString(i));
            if (!existingMethodNames.contains(actualMethodName)) {
                existingMethodNames.add(actualMethodName);
                break;
            }
        }

        return resourceClass.method(JMod.NONE, returnType, actualMethodName);
    }

    @SuppressWarnings("unchecked")
    private void addHttpMethodAnnotation(final String httpMethod, final JAnnotatable annotatable) {
        final Object annotationClass = httpMethodAnnotations.get(httpMethod.toUpperCase());
        annotatable.annotate((Class<? extends Annotation>) annotationClass);
    }

}
