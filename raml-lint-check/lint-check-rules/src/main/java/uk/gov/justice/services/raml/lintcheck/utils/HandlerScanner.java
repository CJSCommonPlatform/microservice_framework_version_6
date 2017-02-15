package uk.gov.justice.services.raml.lintcheck.utils;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class HandlerScanner {


    private final String basePackage;

    public HandlerScanner(final String basePackage) {
        this.basePackage = basePackage;
    }

    private Reflections configureReflections(final String basePackage) {
        return new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(new MethodAnnotationsScanner()));
    }

    private List<String> scanForActions(final Reflections reflections) {
        return reflections
                .getMethodsAnnotatedWith(Handles.class)
                .stream()
                .filter(this::isServiceComponent)
                .map(this::actionName)
                .collect(toList());
    }

    private String actionName(final Method method) {
        return method.getAnnotation(Handles.class).value();
    }

    private boolean isServiceComponent(final Method method) {
        return method.getDeclaringClass()
                .isAnnotationPresent(ServiceComponent.class);
    }

    public Collection<String> getHandlesActions() {
        return scanForActions(configureReflections(basePackage));
    }
}