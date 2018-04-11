package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtil;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SubscriptionMediaTypeToSchemaIdGeneratorTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    private JavaCompilerUtil compiler;

    @Before
    public void before() {
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapper() throws Exception {

        final String eventName_1 = "ctx.command.command1";
        final String mediaType_1 = "application/vnd." +  eventName_1 + "+json";
        final String schemaId_1 = "http://justice.gov.uk/test/schema1.json";

        final String eventName_2 = "ctx.command.command2";
        final String mediaType_2 = "application/vnd." +  eventName_2 + "+json";
        final String schemaId_2 = "http://justice.gov.uk/test/schema2.json";

        final String contextName = "my-context";
        final String componentName = "EVENT_LISTENER";
        final String generatedClassName = "MyContextEventListenerMediaTypeToSchemaIdMapper";

        final List<Event> events = asList(
                new Event(eventName_1, schemaId_1),
                new Event(eventName_2, schemaId_2)
        );
        new SubscriptionMediaTypeToSchemaIdGenerator().generateMediaTypeToSchemaIdMapper(
                contextName,
                componentName,
                events,
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> schemaIdMapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", generatedClassName);
        final MediaTypeToSchemaIdMapper instance = (MediaTypeToSchemaIdMapper) schemaIdMapperClass.newInstance();

        final Map<MediaType, String> mediaTypeToSchemaIdMap = instance.getMediaTypeToSchemaIdMap();

        assertThat(mediaTypeToSchemaIdMap.size(), is(2));
        assertThat(mediaTypeToSchemaIdMap.get(new MediaType(mediaType_1)), is(schemaId_1));
        assertThat(mediaTypeToSchemaIdMap.get(new MediaType(mediaType_2)), is(schemaId_2));
    }
}
