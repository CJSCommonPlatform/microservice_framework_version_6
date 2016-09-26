package uk.gov.justice.services.core.aggregate.util;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.generators.test.utils.compiler.JavaCompilerUtil;

import java.io.File;
import java.io.IOException;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class DynamicAggregateTestClassGenerator {

    public static Class generatedTestAggregateClassOf(long serialVersionUid, String basePackage, String fileNameToBeGenerated) throws IOException, ClassNotFoundException {

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();


        MethodSpec apply = MethodSpec.methodBuilder("apply").addModifiers(Modifier.PUBLIC)
                .addCode("  return event;")
                .returns(Object.class)
                .addAnnotation(Override.class)
                .addParameter(Object.class, "event")
                .build();

        FieldSpec.Builder fieldSpecSerialVersionId = FieldSpec.builder(TypeName.LONG.unbox(), "serialVersionUID", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        fieldSpecSerialVersionId.initializer(serialVersionUid + "L");

        TypeSpec dynamicTestAggregate = TypeSpec.classBuilder(fileNameToBeGenerated)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addField(fieldSpecSerialVersionId.build())
                .addMethod(apply)
                .addSuperinterface(Aggregate.class)
                .build();

        JavaFile javaFile = JavaFile.builder(basePackage, dynamicTestAggregate)
                .build();

        String path = DynamicAggregateTestClassGenerator.class.getClassLoader().getResource("").getPath();
        File pathRoot = new File(path);
        javaFile.writeTo(pathRoot);
        javaFile.writeTo(System.out);
        JavaCompilerUtil compiler = new JavaCompilerUtil(pathRoot, pathRoot);
        Class<?> generatedClass = compiler.compiledClassOf(basePackage, fileNameToBeGenerated);
        return Class.forName(generatedClass.getName());
    }

}
