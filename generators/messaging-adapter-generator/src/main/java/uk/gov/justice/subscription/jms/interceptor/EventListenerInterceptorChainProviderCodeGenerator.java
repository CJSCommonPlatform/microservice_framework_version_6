package uk.gov.justice.subscription.jms.interceptor;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.subscription.jms.core.ClassNameFactory.EVENT_FILTER_INTERCEPTOR;
import static uk.gov.justice.subscription.jms.core.ClassNameFactory.EVENT_LISTENER_INTERCEPTOR_CHAIN_PROVIDER;

import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;
import uk.gov.justice.subscription.jms.core.ClassNameFactory;

import java.util.List;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Should generate a custom EventListenerInterceptorChainProvider that uses a custom generated
 * EventFilterInterceptor.
 *
 * Something like this:
 *
 * <pre>
 *  {@code
 *
 *     public class MyCustomEventListenerInterceptorChainProvider implements InterceptorChainEntryProvider {
 *
 *          private final List<InterceptorChainEntry> interceptorChainEntries = new ArrayList<>();
 *
 *          public ExampleEventListenerInterceptorChainProvider() {
 *              interceptorChainEntries.add(new InterceptorChainEntry(2000, MyCustomEventFilterInterceptor.class));
 *          }
 *
 *          @literal @Override
 *          public String component() {
 *              return "MY_CUSTOM_EVENT_LISTENER";
 *          }
 *
 *          @literal @Override
 *          public List<InterceptorChainEntry> interceptorChainTypes() {
 *              return interceptorChainEntries;
 *          }
 *     }
 *
 * }
 * </pre>
 */
public class EventListenerInterceptorChainProviderCodeGenerator {

    public TypeSpec generate(final String componentName,
                             final ClassNameFactory classNameFactory) {

        final ClassName className = classNameFactory.classNameFor(EVENT_LISTENER_INTERCEPTOR_CHAIN_PROVIDER);
        final ClassName eventFilterInterceptorClassName = classNameFactory.classNameFor(EVENT_FILTER_INTERCEPTOR);

        return classBuilder(className)
                .addModifiers(PUBLIC)
                .addSuperinterface(InterceptorChainEntryProvider.class)
                .addField(createInterceptorChainEntriesMapField())
                .addMethod(createConstructor(eventFilterInterceptorClassName))
                .addMethod(createComponentMethod(componentName))
                .addMethod(createInterceptorChainTypesMethod())
                .build();

    }

    private FieldSpec createInterceptorChainEntriesMapField() {

        final ClassName list = get("java.util", "List");
        final ClassName arrayList = get("java.util", "ArrayList");
        final TypeName listOfInterceptorChainEntries = ParameterizedTypeName.get(list, get(InterceptorChainEntry.class));
        final TypeName arrayListOfInterceptorChainEntries = ParameterizedTypeName.get(arrayList, get(InterceptorChainEntry.class));

        return FieldSpec.builder(listOfInterceptorChainEntries, "interceptorChainEntries", PRIVATE, FINAL)
                .initializer("new $T()", arrayListOfInterceptorChainEntries)
                .build();

    }

    private MethodSpec createConstructor(final ClassName eventFilterInterceptorClassName) {

        final ClassName interceptorChainEntryClassName = get(InterceptorChainEntry.class);

        return constructorBuilder()
                .addModifiers(PUBLIC)
                .addStatement("interceptorChainEntries.add(new $T(2000, $T.class))", interceptorChainEntryClassName, eventFilterInterceptorClassName)
                .build();
    }

    private MethodSpec createComponentMethod(final String componentName) {
        return methodBuilder("component")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return \"$L\"", componentName)
                .returns(String.class)
                .build();
    }

    private MethodSpec createInterceptorChainTypesMethod() {

        final ParameterizedTypeName listOfInterceptorChainEntries = ParameterizedTypeName.get(
                List.class,
                InterceptorChainEntry.class);

        return methodBuilder("interceptorChainTypes")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .returns(listOfInterceptorChainEntries)
                .addStatement("return interceptorChainEntries")
                .build();
    }
}
