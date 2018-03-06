package uk.gov.justice.raml.jms.interceptor;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import uk.gov.justice.raml.jms.core.ClassNameFactory;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Should generate a custom EventFilterInterceptor that uses a custom Event Filter. Something like this:
 *
 * <pre>
 *  {@code
 *
 *     public class MyCustomEventFilterInterceptor implements Interceptor {
 *
 *         @literal @Inject
 *         private MyCustomEventFilter eventFilter;
 *
 *         public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
 *
 *             if (eventFilter.accepts(interceptorContext.inputEnvelope().metadata().name())) {
 *                 return interceptorChain.processNext(interceptorContext);
 *              }
 *
 *             return interceptorContext;
 *         }
 *     }
 *
 *
 * }
 * </pre>
 */
public class EventFilterInterceptorCodeGenerator {

    private static final String CLASS_NAME_SUFFIX = "EventFilterInterceptor";
    private static final String EVENT_FILTER_FIELD_NAME = "eventFilter";

    private final EventFilterFieldCodeGenerator eventFilterFieldCodeGenerator = new EventFilterFieldCodeGenerator();

    /**
     * Generate a custom EventFilterInterceptor which uses a custom {@see uk.gov.justice.services.event.buffer.api.EventFilter}
     *
     * @param eventFilterClassName The class name of the custom EventFilter
     * @param classNameFactory     creates the class name for this generated class
     * @return a TypeSpec that will generate the java source file
     */
    public TypeSpec generate(final ClassName eventFilterClassName,
                             final ClassNameFactory classNameFactory) {

        return classBuilder(classNameFactory.classNameWith(CLASS_NAME_SUFFIX))
                .addModifiers(PUBLIC)
                .addSuperinterface(Interceptor.class)
                .addField(eventFilterFieldCodeGenerator.createEventFilterField(eventFilterClassName))
                .addMethod(createProcessMethod())
                .build();
    }

    private MethodSpec createProcessMethod() {
        final ClassName interceptorContextClassName = get(InterceptorContext.class);

        return MethodSpec.methodBuilder("process")
                .addModifiers(PUBLIC)
                .addParameter(interceptorContextClassName, "interceptorContext", FINAL)
                .addParameter(get(InterceptorChain.class), "interceptorChain", FINAL)
                .returns(interceptorContextClassName)
                .addCode(
                        "if($N.accepts(interceptorContext.inputEnvelope().metadata().name())) {\n" +
                                "    return interceptorChain.processNext(interceptorContext);\n" +
                                "}\n",
                        EVENT_FILTER_FIELD_NAME)
                .addStatement("return interceptorContext")
                .build();
    }
}
