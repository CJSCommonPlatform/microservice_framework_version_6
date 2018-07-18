package uk.gov.justice.domain.aggregate.classloader;

import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class DynamicAggregateTestClassGenerator {

    @SuppressWarnings("unchecked")
    public <T extends Aggregate> Class<T> generatedTestAggregateClassOf(long serialVersionUid, String basePackage, String fileNameToBeGenerated) throws IOException, ClassNotFoundException {

        final MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();

        final MethodSpec apply = MethodSpec.methodBuilder("apply").addModifiers(Modifier.PUBLIC)

                .addCode("        return match(event).with(\n" +
                        "                when(uk.gov.justice.domain.event.EventA.class).apply(x -> {\n" +
                        "                            ++count;\n" +
                        "                        }\n" +
                        "                ));")
                .returns(Object.class)
                .addAnnotation(Override.class)
                .addParameter(Object.class, "event")
                .build();

        final MethodSpec applyObjectStream = MethodSpec.methodBuilder("apply").addModifiers(Modifier.PUBLIC)

                .addCode("        return events\n" +
                        "                    .map(this::apply)\n" +
                        "                    .collect(toList())\n" +
                        "                    .stream();")
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(Stream.class, Object.class))
                .addParameter(ParameterizedTypeName.get(Stream.class, Object.class), "events")
                .build();

        final FieldSpec.Builder fieldSpecSerialVersionId = FieldSpec.builder(TypeName.LONG.unbox(), "serialVersionUID", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        fieldSpecSerialVersionId.initializer(serialVersionUid + "L");

        final FieldSpec.Builder fieldSpecCount = FieldSpec.builder(TypeName.INT.unbox(), "count", Modifier.PRIVATE);


        final TypeSpec dynamicTestAggregate = TypeSpec.classBuilder(fileNameToBeGenerated)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addField(fieldSpecSerialVersionId.build())
                .addField(fieldSpecCount.build())
                .addMethod(apply)
                .addMethod(applyObjectStream)
                .addSuperinterface(Aggregate.class)
                .build();

        final ClassName eventSwitcherMatch = ClassName.get("uk.gov.justice.domain.aggregate", "matcher", "EventSwitcher");
        final ClassName eventSwitcherWhen = ClassName.get("uk.gov.justice.domain.aggregate", "matcher", "EventSwitcher");
        final ClassName toList = ClassName.get("java.util", "stream", "Collectors");
        final JavaFile javaFile = JavaFile.builder(basePackage, dynamicTestAggregate)
                .addStaticImport(eventSwitcherMatch, "match")
                .addStaticImport(eventSwitcherWhen, "when")
                .addStaticImport(toList, "toList")
                .build();

        final String path = DynamicAggregateTestClassGenerator.class.getClassLoader().getResource("").getPath();
        final File pathRoot = new File(path);
        javaFile.writeTo(pathRoot);
        javaFile.writeTo(System.out);
        final Class<?> generatedClass = javaCompilerUtil().compiledClassOf(pathRoot, pathRoot, basePackage, fileNameToBeGenerated);
        return (Class<T>) generatedClass;
    }
}
