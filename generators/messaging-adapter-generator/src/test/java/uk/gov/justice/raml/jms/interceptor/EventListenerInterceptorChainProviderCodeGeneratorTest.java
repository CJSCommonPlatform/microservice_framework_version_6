package uk.gov.justice.raml.jms.interceptor;

import static com.squareup.javapoet.JavaFile.builder;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.components.event.listener.interceptors.EventBufferInterceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;

import java.io.File;
import java.util.List;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventListenerInterceptorChainProviderCodeGeneratorTest {

    private static final File CODE_GENERATION_OUTPUT_DIRECTORY = new File("./target/test-generation");
    private static final File COMPILATION_OUTPUT_DIRECTORY = new File(System.getProperty("java.io.tmpdir"), "interceptorChainProvider-generation");

    @InjectMocks
    private EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator;

    @Test
    public void shouldGenerateAWorkingEventListenerInterceptorChainProviderWithTheCorrectInterceptorChainEntiresAndComponentName() throws Exception {

        final String componentName = "MY_CUSTOM_EVENT_LISTENER";

        final String packageName = "uk.gov.justice.api.interceptor.filter";
        final String simpleName = "MyCustomEventListenerInterceptorChainProvider";

        final ClassName eventFilterInterceptorClassName = ClassName.get(StubEventFilterInterceptor.class);

        final TypeSpec typeSpec = eventListenerInterceptorChainProviderCodeGenerator.generate(
                eventFilterInterceptorClassName,
                componentName);

        final File codeGenerationOutputDirectory = getDirectory(CODE_GENERATION_OUTPUT_DIRECTORY);
        final File compilationOutputDirectory = getDirectory(COMPILATION_OUTPUT_DIRECTORY);

        builder(packageName, typeSpec)
                .build()
                .writeTo(codeGenerationOutputDirectory);

        final JavaCompilerUtil compiler = new JavaCompilerUtil(codeGenerationOutputDirectory, compilationOutputDirectory);
        final Class<?> compiledClass = compiler.compiledClassOf(packageName, simpleName);

        final InterceptorChainEntryProvider interceptorChainEntryProvider = (InterceptorChainEntryProvider) compiledClass.newInstance();

        assertThat(interceptorChainEntryProvider.component(), is(componentName));

        final List<InterceptorChainEntry> interceptorChainEntries = interceptorChainEntryProvider.interceptorChainTypes();
        assertThat(interceptorChainEntries, hasItem(new InterceptorChainEntry(1000, EventBufferInterceptor.class)));
        assertThat(interceptorChainEntries, hasItem(new InterceptorChainEntry(2000, StubEventFilterInterceptor.class)));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "SameParameterValue"})
    private File getDirectory(final File aTemporaryDirectory) {

        if (aTemporaryDirectory.exists()) {
            aTemporaryDirectory.delete();
        }

        aTemporaryDirectory.mkdirs();

        return aTemporaryDirectory;
    }
}
