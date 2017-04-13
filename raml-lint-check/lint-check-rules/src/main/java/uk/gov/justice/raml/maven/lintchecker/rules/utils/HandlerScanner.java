package uk.gov.justice.raml.maven.lintchecker.rules.utils;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.raml.maven.lintchecker.LintCheckPluginException;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class HandlerScanner {

    public HandlerScanner() {

    }

    private Reflections configureReflections() {
        return new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(Thread.currentThread().getContextClassLoader()))
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
                .isAnnotationPresent(ServiceComponent.class)||method.getDeclaringClass()
                .isAnnotationPresent(CustomServiceComponent.class);
    }

    public Collection<String> getHandlesActions(final MavenProject project) throws LintCheckPluginException {

        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Set<URL> urls = new HashSet<>();
            for (String element : project.getRuntimeClasspathElements()) {
                urls.add(new File(element).toURI().toURL());
            }

            final ClassLoader contextClassLoader = URLClassLoader.newInstance(
                    urls.toArray(new URL[0]), originalClassLoader);

            Thread.currentThread().setContextClassLoader(contextClassLoader);

        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new LintCheckPluginException("Could not set up class loader", e);
        }

        final List<String> actions = scanForActions(configureReflections());

        Thread.currentThread().setContextClassLoader(originalClassLoader);

        return actions;

    }
}