package uk.gov.justice.raml.jms.core;


import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.event.buffer.api.AbstractEventFilter;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;

import java.io.File;
import java.util.Map;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JmsEndpointGenerator_EventFilterTest extends BaseGeneratorTest {

    private static final String BASE_PACKAGE = "uk.test";
    private static final String BASE_PACKAGE_FOLDER = "/uk/test";

    @Mock
    private JmsProcessor jmsProcessor;

    private Map<String, String> generatorProperties;

    @Before
    public void setup() throws Exception {
        super.before();
        generator = new JmsEndpointGenerator();
        generatorProperties = generatorProperties().withServiceComponentOf(EVENT_LISTENER).build();
    }

    @Test
    public void shouldGenerateEventFilterForEventListener() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/structure")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventA+json")
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventB+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "StructureEventListenerEventFilter");

        final AbstractEventFilter eventFilter = (AbstractEventFilter) clazz.newInstance();

        assertThat(eventFilter.accepts("structure.eventA"), is(true));
        assertThat(eventFilter.accepts("structure.eventB"), is(true));
        assertThat(eventFilter.accepts("structure.eventC"), is(false));
    }

    @Test
    public void shouldNotGenerateEventFilterForEventProcessor() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/processor/message/structure")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventA+json")
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventB+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        assertThat(asList(packageDir.listFiles()), not(hasItem(hasProperty("name", containsString("EventFilter")))));
    }

    @Test
    public void shouldNotGenerateEventFilterForGeneralMediaType() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/context")
                        .with(resource()
                                .withRelativeUri("/some.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getRoot().getAbsolutePath() + BASE_PACKAGE_FOLDER);
        assertThat(asList(packageDir.listFiles()), not(hasItem(hasProperty("name", containsString("EventFilter")))));
    }

    @Test
    public void shouldAddDIAnnotations() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/structure")
                        .with(resource().withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "StructureEventListenerEventFilter");
        assertThat(clazz.getAnnotation(ApplicationScoped.class), is(not(nullValue())));
        assertThat(clazz.getAnnotation(Alternative.class), is(not(nullValue())));
        final Priority priorityAnnotation = clazz.getAnnotation(Priority.class);
        assertThat(priorityAnnotation, is(not(nullValue())));
        assertThat(priorityAnnotation.value(), is(2));
    }

    @Test
    public void shouldGenerateEventFilterIfBaseUriContainsSpecialCharacters() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/my-hyphenated-service")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventA+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        Class<?> clazz = compiler.compiledClassOf(BASE_PACKAGE, "MyHyphenatedServiceEventListenerEventFilter");

        final AbstractEventFilter eventFilter = (AbstractEventFilter) clazz.newInstance();

        assertThat(eventFilter.accepts("structure.eventA"), is(true));
    }
}
