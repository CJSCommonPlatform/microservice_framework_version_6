package uk.gov.justice.services.adapters.rest.generator;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;
import static uk.gov.justice.services.generators.test.utils.reflection.ReflectionUtil.methodOf;

import java.lang.reflect.Method;

import javax.inject.Named;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RestAdapterGenerator_ActionMapperTest extends BaseRestAdapterGeneratorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnActionNameForGETResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withResponseType("application/vnd.ctx.query.somemediatype1+json"))
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withResponseType("application/vnd.ctx.query.somemediatype2+json"))
                                        .with(mapping()
                                                .withName("contextA.someOtherAction")
                                                .withResponseType("application/vnd.ctx.query.somemediatype3+json"))
                                        .withResponseTypes("application/vnd.ctx.query.somemediatype1+json",
                                                "application/vnd.ctx.query.somemediatype2+json",
                                                "application/vnd.ctx.query.somemediatype3+json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultUserResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "getUser", "GET",
                headersWith("Accept", "application/vnd.ctx.query.somemediatype1+json"));
        assertThat(action, is("contextA.someAction"));

        action = actionMethod.invoke(mapperObject, "getUser", "GET",
                headersWith("Accept", "application/vnd.ctx.query.somemediatype2+json"));
        assertThat(action, is("contextA.someAction"));

        action = actionMethod.invoke(mapperObject, "getUser", "GET",
                headersWith("Accept", "application/vnd.ctx.query.somemediatype3+json"));
        assertThat(action, is("contextA.someOtherAction"));

    }

    @Test
    public void shouldReturnActionNameForGETResource2() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/status")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("ctxA.actionA")
                                                .withResponseType("application/vnd.ctx.query.mediatype1+json")
                                        )
                                        .withResponseTypes("application/vnd.ctx.query.mediatype1+json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultStatusResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "getStatus", "GET",
                headersWith("Accept", "application/vnd.ctx.query.mediatype1+json"));
        assertThat(action, is("ctxA.actionA"));
    }

    @Test
    public void shouldReturnActionNameForPOSTResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(POST)
                                        .with(mapping()
                                                .withName("contextB.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype1+json"))
                                        .with(mapping()
                                                .withName("contextB.someOtherAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype2+json"))
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json", "json/schema/somemediatype1.json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json", "json/schema/somemediatype2.json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "postContextBSomeActionCase", "POST",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype1+json"));
        assertThat(action, is("contextB.someAction"));

        action = actionMethod.invoke(mapperObject, "postContextBSomeOtherActionCase", "POST",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype2+json"));
        assertThat(action, is("contextB.someOtherAction"));

    }

    @Test
    public void shouldReturnActionNameForPUTResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(PUT)
                                        .with(mapping()
                                                .withName("contextB.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype1+json"))
                                        .with(mapping()
                                                .withName("contextB.someOtherAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype2+json"))
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json", "json/schema/somemediatype1.json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json", "json/schema/somemediatype2.json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        final Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        final Object mapperObject = mapperClass.newInstance();
        final Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        final Object firstAction = actionMethod.invoke(mapperObject, "putContextBSomeActionCase", "PUT",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype1+json"));
        assertThat(firstAction, is("contextB.someAction"));

        final Object secondAction = actionMethod.invoke(mapperObject, "putContextBSomeOtherActionCase", "PUT",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype2+json"));
        assertThat(secondAction, is("contextB.someOtherAction"));

    }

    @Test
    public void shouldReturnActionNameForPATCHResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(PATCH)
                                        .with(mapping()
                                                .withName("contextB.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype1+json"))
                                        .with(mapping()
                                                .withName("contextB.someOtherAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype2+json"))
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json", "json/schema/somemediatype1.json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json", "json/schema/somemediatype2.json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        final Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        final Object mapperObject = mapperClass.newInstance();
        final Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        final Object firstAction = actionMethod.invoke(mapperObject, "patchContextBSomeActionCase", "PATCH",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype1+json"));
        assertThat(firstAction, is("contextB.someAction"));

        final Object secondAction = actionMethod.invoke(mapperObject, "patchContextBSomeOtherActionCase", "PATCH",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype2+json"));
        assertThat(secondAction, is("contextB.someOtherAction"));

    }

    @Test
    public void shouldReturnActionNameForPOSTResourceSameActionMappedToTwoMediaTypes() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(POST)
                                        .with(mapping()
                                                .withName("contextC.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype1+json"))
                                        .with(mapping()
                                                .withName("contextD.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype2+json"))
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json", "json/schema/somemediatype1.json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json", "json/schema/somemediatype2.json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "postContextCSomeActionCase", "POST",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype1+json"));
        assertThat(action, is("contextC.someAction"));

        action = actionMethod.invoke(mapperObject, "postContextDSomeActionCase", "POST",
                headersWith("Content-Type", "application/vnd.ctx.command.somemediatype2+json"));
        assertThat(action, is("contextD.someAction"));

    }

    @Test
    public void shouldReturnActionNameForPOSTAndGETResource() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(POST)
                                        .with(mapping()
                                                .withName("contextC.commandAction")
                                                .withRequestType("application/vnd.somemediatype1+json"))
                                        .withMediaType("application/vnd.somemediatype1+json", "json/schema/somemediatype1.json")
                                )
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("contextC.queryAction")
                                                .withResponseType("application/vnd.mediatype1+json")
                                        )
                                        .withResponseTypes("application/vnd.mediatype1+json")
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultCaseResourceActionMapper");
        Object mapperObject = mapperClass.newInstance();
        Method actionMethod = methodOf(mapperClass.getSuperclass(), "actionOf");

        Object action = actionMethod.invoke(mapperObject, "postContextCCommandActionCase", "POST",
                headersWith("Content-Type", "application/vnd.somemediatype1+json"));
        assertThat(action, is("contextC.commandAction"));

        action = actionMethod.invoke(mapperObject, "getCase", "GET",
                headersWith("Accept", "application/vnd.mediatype1+json"));
        assertThat(action, is("contextC.queryAction"));

    }

    @Test
    public void shouldContainNamedAnnotation() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .with(resource("/status")
                                .with(defaultGetAction())

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties().build()));

        Class<?> mapperClass = compiler.compiledClassOf(BASE_PACKAGE, "mapper", "DefaultStatusResourceActionMapper");
        assertThat(mapperClass.getAnnotation(Named.class), not(nullValue()));
        assertThat(mapperClass.getAnnotation(Named.class).value(), Matchers.is("DefaultStatusResourceActionMapper"));

    }
}
