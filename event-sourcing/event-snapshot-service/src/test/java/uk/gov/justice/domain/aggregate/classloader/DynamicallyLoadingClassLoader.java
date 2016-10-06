package uk.gov.justice.domain.aggregate.classloader;

import java.io.IOException;
import java.io.InputStream;

public class DynamicallyLoadingClassLoader extends ClassLoader {
    private final String path;
    private final Class clazz;
    private final String classNameToBeLoadedDynamically;

    public DynamicallyLoadingClassLoader(final Class clazz,
                                         final String classNameToBeLoadedDynamically,
                                         final String pathToTheClassFile) {
        this.clazz = clazz;
        this.path = pathToTheClassFile;
        this.classNameToBeLoadedDynamically = classNameToBeLoadedDynamically;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        if (name.contains(classNameToBeLoadedDynamically)) {
            try {
                final InputStream is = clazz.getClassLoader().getResourceAsStream(path);
                byte[] buf = new byte[10000];
                int len = is.read(buf);
                return defineClass(name, buf, 0, len);
            } catch (IOException e) {
                throw new ClassNotFoundException("Failed to dynamically load the class: " + name, e);
            }
        } else {
            return getParent().loadClass(name);
        }
    }
}