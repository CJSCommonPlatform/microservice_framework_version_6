package uk.gov.justice.services.adapters.rest.generator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithNoMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodOf;

import java.lang.reflect.Method;

import javax.inject.Named;

import org.hamcrest.Matchers;
import org.junit.Test;

public class RestAdapterGenerator_MediaTypeMapperTest extends BaseRestAdapterGeneratorTest {

    @Test
    public void shouldReturnActionNameForGETResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/status")
                                .with(httpActionWithNoMapping(GET)
                                        .withResponseTypes("application/vnd.ctx.query.mediatype1+json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ACTION_MAPPING_FALSE));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultStatusResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "getStatus", "GET",
                headersWith("Accept", "application/vnd.ctx.query.mediatype1+json"));
        assertThat(action, is("ctx.query.mediatype1"));
    }

    @Test
    public void shouldReturnActionNameForPOSTResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpActionWithNoMapping(POST)
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ACTION_MAPPING_FALSE));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "postCtxCommandSomemediatype1Case", "POST",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype1+json"));
        assertThat(action, is("ctx.command.somemediatype1"));

        action = actionMethod.invoke(mapperObject, "postCtxCommandSomemediatype2Case", "POST",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype2+json"));
        assertThat(action, is("ctx.command.somemediatype2"));
    }

    @Test
    public void shouldReturnActionNameForPOSTAndGETResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpActionWithNoMapping(POST)
                                        .withMediaType("application/vnd.contextc.command.somemediatype1+json")
                                )
                                .with(httpActionWithNoMapping(GET)
                                        .withResponseTypes("application/vnd.contextc.query.mediatype1+json")
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ACTION_MAPPING_FALSE));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "postContextcCommandSomemediatype1Case", "POST",
                headersWith("Content-Type", "application/vnd.contextc.command.somemediatype1+json"));
        assertThat(action, is("contextc.command.somemediatype1"));

        action = actionMethod.invoke(mapperObject, "getCase", "GET",
                headersWith("Accept", "application/vnd.contextc.query.mediatype1+json"));
        assertThat(action, is("contextc.query.mediatype1"));
    }

    @Test
    public void shouldContainNamedAnnotation() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/status")
                                .with(httpActionWithNoMapping(GET)
                                        .withDefaultResponseType())

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, ACTION_MAPPING_FALSE));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultStatusResourceActionMapper");
        assertThat(mapperClass.getAnnotation(Named.class), not(nullValue()));
        assertThat(mapperClass.getAnnotation(Named.class).value(), Matchers.is("DefaultStatusResourceActionMapper"));
    }

}
