package uk.gov.justice.domain.aggregate.classloader;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.generators.test.utils.compiler.JavaCompilerUtil;

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

    public <T extends Aggregate> Class<T> generatedTestAggregateClassOf(long serialVersionUid, String basePackage, String fileNameToBeGenerated) throws IOException, ClassNotFoundException {


        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();

        MethodSpec apply = MethodSpec.methodBuilder("apply").addModifiers(Modifier.PUBLIC)

                .addCode("        return match(event).with(\n" +
                        "                when(uk.gov.justice.domain.event.EventA.class).apply(x -> {\n" +
                        "                            ++count;\n" +
                        "                        }\n" +
                        "                ));")
                .returns(Object.class)
                .addAnnotation(Override.class)
                .addParameter(Object.class, "event")
                .build();

        ClassName streamOfObjects = ClassName.get("uk.gov.justice.domain.aggregate", "matcher", "EventSwitcher");

        MethodSpec applyObjectStream = MethodSpec.methodBuilder("apply").addModifiers(Modifier.PUBLIC)

                .addCode("        return events\n" +
                        "                    .map(this::apply)\n" +
                        "                    .collect(toList())\n" +
                        "                    .stream();")
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(Stream.class, Object.class))
                .addParameter(ParameterizedTypeName.get(Stream.class, Object.class), "events")
                .build();

        FieldSpec.Builder fieldSpecSerialVersionId = FieldSpec.builder(TypeName.LONG.unbox(), "serialVersionUID", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        fieldSpecSerialVersionId.initializer(serialVersionUid + "L");

        FieldSpec.Builder fieldSpecCount = FieldSpec.builder(TypeName.INT.unbox(), "count", Modifier.PRIVATE);


        TypeSpec dynamicTestAggregate = TypeSpec.classBuilder(fileNameToBeGenerated)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addField(fieldSpecSerialVersionId.build())
                .addField(fieldSpecCount.build())
                .addMethod(apply)
                .addMethod(applyObjectStream)
                .addSuperinterface(Aggregate.class)
                .build();

        ClassName eventSwitcherMatch = ClassName.get("uk.gov.justice.domain.aggregate", "matcher", "EventSwitcher");
        ClassName eventSwitcherWhen = ClassName.get("uk.gov.justice.domain.aggregate", "matcher", "EventSwitcher");
        ClassName toList = ClassName.get("java.util", "stream", "Collectors");
        JavaFile javaFile = JavaFile.builder(basePackage, dynamicTestAggregate)
                .addStaticImport(eventSwitcherMatch, "match")
                .addStaticImport(eventSwitcherWhen, "when")
                .addStaticImport(toList, "toList")
                .build();

        String path = DynamicAggregateTestClassGenerator.class.getClassLoader().getResource("").getPath();
        File pathRoot = new File(path);
        javaFile.writeTo(pathRoot);
        javaFile.writeTo(System.out);
        JavaCompilerUtil compiler = new JavaCompilerUtil(pathRoot, pathRoot);
        Class<?> generatedClass = compiler.compiledClassOf(basePackage, fileNameToBeGenerated);
        return (Class<T>) generatedClass;
    }

}
