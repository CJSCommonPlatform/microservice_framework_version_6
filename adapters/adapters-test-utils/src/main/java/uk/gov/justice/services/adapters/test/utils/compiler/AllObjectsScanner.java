package uk.gov.justice.services.adapters.test.utils.compiler;

import org.reflections.scanners.SubTypesScanner;

/**
 * Extension of the reflection API to scan for all classes
 *
 */
public class AllObjectsScanner extends SubTypesScanner {

    public AllObjectsScanner() {
        super(false);
    }


    /**
     * Accepts all classes and puts them in the store as subclasses of @Object
     *
     */
    @SuppressWarnings({"unchecked"})
    public void scan(final Object cls) {
        String className = getMetadataAdapter().getClassName(cls);
        String superclass = Object.class.getName();

        if (acceptResult(superclass)) {
            getStore().put(superclass, className);
        }
    }

}
