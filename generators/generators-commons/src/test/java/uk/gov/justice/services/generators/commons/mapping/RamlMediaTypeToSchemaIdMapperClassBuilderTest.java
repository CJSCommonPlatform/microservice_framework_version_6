package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.services.core.annotation.SchemaIdMapper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RamlMediaTypeToSchemaIdMapperClassBuilderTest {

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Mock
    private RamlSchemaMappingClassNameGenerator schemaMappingClassNameGenerator;

    @InjectMocks
    private RamlMediaTypeToSchemaIdMapperClassBuilder ramlMediaTypeToSchemaIdMapperClassBuilder;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGenerateACorrectMapperClass() throws Exception {

        final String baseUri = "http://localhost:8080/people-command-api/command/api/rest/people";
        final String simpleName = "PeopleCommandApiMediaTypeToSchemaIdMapper";
        final String packageName = "uk.gov.justice.generation.mapper.example.generated.test";

        final MediaType mediaType_1 = new MediaType("application/vnd.ctx.command.mediaType_1+json");
        final MediaType mediaType_2 = new MediaType("application/vnd.ctx.command.mediaType_2+json");
        final MediaType mediaType_3 = new MediaType("application/vnd.ctx.command.mediaType_3+json");
        final MediaType mediaType_4 = new MediaType("application/vnd.ctx.command.mediaType_4+json");

        final List<MediaTypeToSchemaId> mediaTypeToSchemaIds = asList(
                new MediaTypeToSchemaId(mediaType_1, "schemaId_1"),
                new MediaTypeToSchemaId(mediaType_2, "schemaId_2"),
                new MediaTypeToSchemaId(mediaType_3, "schemaId_3"),
                new MediaTypeToSchemaId(mediaType_4, "schemaId_4")
        );

        when(schemaMappingClassNameGenerator.createMappingClassNameFrom(baseUri, MediaTypeToSchemaIdMapper.class)).thenReturn(simpleName);

        final TypeSpec typeSpec = ramlMediaTypeToSchemaIdMapperClassBuilder.typeSpecWith(baseUri, mediaTypeToSchemaIds);

        final Class<?> mapperClass = writeSourceFileAndCompile(packageName, typeSpec);

        assertThat(mapperClass.getAnnotation(SchemaIdMapper.class), is(notNullValue()));

        assertThat(mapperClass.getPackage().getName(), is(packageName));
        assertThat(mapperClass.getSimpleName(), is(simpleName));

        final Constructor<?> constructor = mapperClass.getConstructor();

        final Object instance = constructor.newInstance();

        final Method getSchemaIds = mapperClass.getMethod("getMediaTypeToSchemaIdMap");

        final Map<MediaType, String> schemaIds = (Map<MediaType, String>) getSchemaIds.invoke(instance);

        assertThat(schemaIds.get(mediaType_1), is("schemaId_1"));
        assertThat(schemaIds.get(mediaType_2), is("schemaId_2"));
        assertThat(schemaIds.get(mediaType_3), is("schemaId_3"));
        assertThat(schemaIds.get(mediaType_4), is("schemaId_4"));
    }

    @SuppressWarnings("ConstantConditions")
    private Class<?> writeSourceFileAndCompile(final String packageName, final TypeSpec typeSpec) throws IOException {

        final File outputFolderRoot = outputFolder.getRoot();

        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(outputFolderRoot);


        return javaCompilerUtil()
                .compiledClassesOf(outputFolderRoot, outputFolderRoot, packageName)
                .stream()
                .filter(clazz -> !clazz.getName().equals("java.lang.Object"))
                .findFirst().orElseGet(null);
    }
}
