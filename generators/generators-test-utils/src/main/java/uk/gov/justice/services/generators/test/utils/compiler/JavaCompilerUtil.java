package uk.gov.justice.services.generators.test.utils.compiler;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.join;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toSet;
import static org.reflections.ReflectionUtils.forNames;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles and loads classes and interfaces from the specified folders
 */
public class JavaCompilerUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCompilerUtil.class);
    private final File codegenOutputDir, compilationOutputDir;

    public JavaCompilerUtil(final File codegenOutputDir, final File compilationOutputDir) {
        this.codegenOutputDir = codegenOutputDir;
        this.compilationOutputDir = compilationOutputDir;
    }

    /**
     * Compiles and loads a single class
     *
     * @param basePackage - the base package of the class to be compiled
     * @return the Class
     * @throws IllegalStateException - if more or less than one classes found
     */
    public Class<?> compiledClassOf(final String basePackage) {
        final Set<Class<?>> resourceClasses = compiledClassesOf(basePackage);
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    /**
     * Compiles then finds a single class.
     *
     * @param basePackage              the base package
     * @param additionalFilterElements the additional filter elements
     * @return the class
     * @throws IllegalStateException - if more or less than one classes found
     */
    public Class<?> compiledClassOf(final String basePackage, final String... additionalFilterElements) {
        return compiledClassOf(basePackage, c -> !c.isInterface(), additionalFilterElements);
    }

    /**
     * Compiles then finds a single interface class.
     *
     * @param basePackage              the base package
     * @param additionalFilterElements the additional filter elements
     * @return the class
     * @throws IllegalStateException - if more or less than one classes found
     */
    public Class<?> compiledInterfaceClassOf(final String basePackage, final String... additionalFilterElements) {
        return compiledClassOf(basePackage, Class::isInterface, additionalFilterElements);
    }

    public Class<?> classOf(final Set<Class<?>> classes, final String basePackage, final String... additionalFilterElements) {
        final Set<Class<?>> resourceClasses = classesMatching(classes,
                c -> !c.isInterface() && c.getName().equals(join(".", basePackage, join(".", additionalFilterElements))));
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    /**
     * Compiles and loads a single interface
     *
     * @param basePackageName - the base package of the interface to be compiled
     * @return the Class
     * @throws IllegalStateException - if more or less than one interfaces found
     */
    public Class<?> compiledInterfaceOf(final String basePackageName) {
        final Set<Class<?>> resourceInterfaces = compiledInterfacesOf(basePackageName);
        if (resourceInterfaces.size() != 1) {
            throw new IllegalStateException(
                    format("Expected to find single interface but found {0}", resourceInterfaces));

        }
        return resourceInterfaces.iterator().next();
    }

    /**
     * compiles and loads specified classes
     *
     * @param basePackage - the base package of the classes to be compiled
     * @return the set of classes
     */
    public Set<Class<?>> compiledClassesOf(final String basePackage) {
        return compiledClassesAndInterfaces(c -> !c.isInterface(), basePackage);
    }

    /**
     * compiles and loads specified interfaces
     *
     * @param basePackage - the base package of the interfaces to be compiled
     * @return the set of classes
     */
    public Set<Class<?>> compiledInterfacesOf(final String basePackage) {
        return compiledClassesAndInterfaces(Class::isInterface, basePackage);
    }

    private Class<?> compiledClassOf(final String basePackage,
                                     final Predicate<Class<?>> predicate,
                                     final String... additionalFilterElements) {

        final Set<Class<?>> resourceClasses = compiledClassesAndInterfaces(
                c -> predicate.test(c) && c.getName().equals(join(".", basePackage, join(".", additionalFilterElements))), basePackage);
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    private Set<Class<?>> compiledClassesAndInterfaces(final Predicate<? super Class<?>> predicate,
                                                       final String basePackage) {
        return classesMatching(compile(basePackage), predicate);
    }

    private Set<Class<?>> classesMatching(final Set<Class<?>> classes,
                                          final Predicate<? super Class<?>> predicate) {
        return classes.stream().filter(predicate).collect(toSet());
    }

    private Set<Class<?>> compile(final String basePackage) {
        compile();
        return loadClasses(basePackage);
    }

    private Set<Class<?>> loadClasses(final String basePackage) {
        try (URLClassLoader resourceClassLoader = new URLClassLoader(new URL[]{compilationOutputDir.toURI().toURL()})) {
            final Reflections reflections = new Reflections(basePackage, new SubTypesScanner(false), resourceClassLoader);
            return newHashSet(forNames(getClassNames(reflections), resourceClassLoader));
        } catch (IOException ex) {
            throw new RuntimeException("Error creating class loader", ex);
        }
    }

    private Set<String> getClassNames(final Reflections reflections) {
        Multimap<String, String> types = reflections.getStore().get(SubTypesScanner.class.getSimpleName());
        return Stream.concat(types.values().stream(), types.keySet().stream()).collect(toSet());
    }

    private void compile() {

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);
        final List<JavaFileObject> javaObjects = scanRecursivelyForJavaObjects(codegenOutputDir, fileManager);

        if (javaObjects.size() == 0) {
            throw new CompilationException(
                    format("There are no source files to compile in {0}", codegenOutputDir.getAbsolutePath()));
        }
        final String[] compileOptions = new String[]{"-d", compilationOutputDir.getAbsolutePath()};
        final Iterable<String> compilationOptions = Arrays.asList(compileOptions);

        final CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnostics, compilationOptions, null,
                javaObjects);

        if (!compilerTask.call()) {
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                LOG.error("Error on line {} in {}", diagnostic.getLineNumber(), diagnostic);
            }
            throw new CompilationException("Could not compile project");
        }
    }

    private List<JavaFileObject> scanRecursivelyForJavaObjects(final File dir, final StandardJavaFileManager fileManager) {
        final List<JavaFileObject> javaObjects = new LinkedList<>();
        final File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaObjects.addAll(scanRecursivelyForJavaObjects(file, fileManager));
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
                    try {
                        LOG.debug(FileUtils.readFileToString(file));
                    } catch (IOException e) {
                        LOG.warn("Could not read file", e);
                    }
                    javaObjects.add(readJavaObject(file, fileManager));
                }
            }
        }
        return javaObjects;
    }

    private JavaFileObject readJavaObject(final File file, final StandardJavaFileManager fileManager) {
        final Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(file);
        final Iterator<? extends JavaFileObject> it = javaFileObjects.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new CompilationException(format("Could not load {0} java file object", file.getAbsolutePath()));
    }

}
