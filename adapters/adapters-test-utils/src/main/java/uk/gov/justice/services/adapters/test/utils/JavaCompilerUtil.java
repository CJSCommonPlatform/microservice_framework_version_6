package uk.gov.justice.services.adapters.test.utils;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles and loads classes and interfaces from the specified folders
 */
public class JavaCompilerUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCompilerUtil.class);
    private final File codegenOutputDir, compilationOutputDir;

    public JavaCompilerUtil(File codegenOutputDir, File compilationOutputDir) {
        this.codegenOutputDir = codegenOutputDir;
        this.compilationOutputDir = compilationOutputDir;
    }

    /**
     * Compiles and loads a single class
     *
     * @param classNames
     * @param basePackage
     * @return
     * @throws MalformedURLException
     * @throws IllegalStateException
     *             - if more or less than one classes found
     */
    public Class<?> compiledClassOf(Set<String> classNames, String basePackage)
            throws MalformedURLException {
        Set<Class<?>> resourceClasses = compiledClassesOf(classNames, basePackage);
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    /**
     * Compiles and loads a single interface
     *
     * @param classNames
     * @param basePackageName
     * @return
     * @throws MalformedURLException
     * @throws IllegalStateException
     *             - if more or less than one interfaces found
     */
    public Class<?> compiledInterfaceOf(Set<String> classNames, String basePackageName)
            throws MalformedURLException {
        Set<Class<?>> resourceInterfaces = compiledInterfacesOf(classNames, basePackageName);
        if (resourceInterfaces.size() != 1) {
            throw new IllegalStateException(
                    format("Expected to find single interface but found {0}", resourceInterfaces));

        }
        return resourceInterfaces.iterator().next();
    }

    /**
     * compiles and loads specified classes
     *
     * @param classNames
     * @param basePackage
     * @return
     * @throws MalformedURLException
     */
    public Set<Class<?>> compiledClassesOf(Set<String> classNames, String basePackage)
            throws MalformedURLException {
        return compiledClassesAndInterfaces(classNames, c -> !c.isInterface(), basePackage);
    }

    /**
     * compiles and loads specified interfaces
     *
     * @param resourceClasses
     * @param basePackage
     * @return
     * @throws MalformedURLException
     */
    public Set<Class<?>> compiledInterfacesOf(Set<String> resourceClasses, String basePackage)
            throws MalformedURLException {
        return compiledClassesAndInterfaces(resourceClasses, c -> c.isInterface(), basePackage);
    }

    private Set<Class<?>> compiledClassesAndInterfaces(Set<String> classNames,
            Predicate<? super Class<?>> predicate, String basePackage)
                    throws MalformedURLException {
        return compile(classNames, basePackage).stream().filter(predicate).collect(Collectors.toSet());
    }

    private Set<Class<?>> compile(Set<String> classNames, String basePackage) throws MalformedURLException {
        compile(classNames);
        return loadClasses(basePackage);
    }

    private Set<Class<?>> loadClasses(String basePackage) throws MalformedURLException {
        URLClassLoader resourceClassLoader = new URLClassLoader(new URL[] { compilationOutputDir.toURI().toURL() });

        ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
        Set<Class<?>> rootResourceClasses = new HashSet<>();
        try {
            Thread.currentThread().setContextClassLoader(resourceClassLoader);
            Reflections reflections = new Reflections(basePackage, ClasspathHelper.forClass(Object.class),
                    new SubTypesScanner(false));
            rootResourceClasses.addAll(reflections.getSubTypesOf(Object.class));
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
        }
        return rootResourceClasses;
    }

    private void compile(Set<String> resourceClasses) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);
        List<JavaFileObject> javaObjects = scanRecursivelyForJavaObjects(codegenOutputDir, fileManager);

        if (javaObjects.size() == 0) {
            throw new CompilationException(
                    format("There are no source files to compile in {0}", codegenOutputDir.getAbsolutePath()));
        }
        String[] compileOptions = new String[] { "-d", compilationOutputDir.getAbsolutePath() };
        Iterable<String> compilationOptions = Arrays.asList(compileOptions);

        CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnostics, compilationOptions, null,
                javaObjects);

        if (!compilerTask.call()) {
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                LOG.error("Error on line {} in {}", diagnostic.getLineNumber(), diagnostic);
            }
            throw new CompilationException("Could not compile project");
        }
    }

    private List<JavaFileObject> scanRecursivelyForJavaObjects(File dir, StandardJavaFileManager fileManager) {
        List<JavaFileObject> javaObjects = new LinkedList<JavaFileObject>();
        File[] files = dir.listFiles();
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
        return javaObjects;
    }

    private JavaFileObject readJavaObject(File file, StandardJavaFileManager fileManager) {
        Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(file);
        Iterator<? extends JavaFileObject> it = javaFileObjects.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new CompilationException(format("Could not load {0} java file object", file.getAbsolutePath()));
    }

}
