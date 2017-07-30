package com.camnter.smartrounter.complier.annotation;

import com.camnter.smartrounter.complier.RouterType;
import com.camnter.smartrounter.complier.core.BaseAnnotatedClass;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.camnter.smartrounter.complier.RouterType.ANDROID_INTENT;
import static com.camnter.smartrounter.complier.RouterType.BOOLEAN;
import static com.camnter.smartrounter.complier.RouterType.BOXED_BOOLEAN;
import static com.camnter.smartrounter.complier.RouterType.BOXED_BYTE;
import static com.camnter.smartrounter.complier.RouterType.BOXED_CHAR;
import static com.camnter.smartrounter.complier.RouterType.BOXED_DOUBLE;
import static com.camnter.smartrounter.complier.RouterType.BOXED_FLOAT;
import static com.camnter.smartrounter.complier.RouterType.BOXED_INT;
import static com.camnter.smartrounter.complier.RouterType.BOXED_LONG;
import static com.camnter.smartrounter.complier.RouterType.BOXED_SHORT;
import static com.camnter.smartrounter.complier.RouterType.BYTE;
import static com.camnter.smartrounter.complier.RouterType.CHAR;
import static com.camnter.smartrounter.complier.RouterType.DOUBLE;
import static com.camnter.smartrounter.complier.RouterType.FLOAT;
import static com.camnter.smartrounter.complier.RouterType.INT;
import static com.camnter.smartrounter.complier.RouterType.LONG;
import static com.camnter.smartrounter.complier.RouterType.SHORT;
import static com.camnter.smartrounter.complier.RouterType.SMART_ROUTERS;
import static com.camnter.smartrounter.complier.RouterType.STRING;

/**
 * @author CaMnter
 */

public class AnnotatedClass extends BaseAnnotatedClass {

    private final List<RouterHostAnnotation> routerHostAnnotationList;
    private final List<RouterFieldAnnotation> routerFieldAnnotationList;


    public AnnotatedClass(Element annotatedElement,
                          Elements elements,
                          String fullClassName) {
        super(annotatedElement, elements, fullClassName);
        this.routerHostAnnotationList = new ArrayList<>();
        this.routerFieldAnnotationList = new ArrayList<>();
    }


    public void addRouterHostAnnotation(RouterHostAnnotation routerHostAnnotation) {
        this.routerHostAnnotationList.add(routerHostAnnotation);
    }


    public void addRouterFieldAnnotation(RouterFieldAnnotation routerFieldAnnotation) {
        this.routerFieldAnnotationList.add(routerFieldAnnotation);
    }


    /**
     * get the JavaFile
     *
     * @return JavaFile
     */
    @Override
    public JavaFile javaFile() {

        final String className = this.annotatedElementSimpleName + "_SmartRouter";

        /*
         * _SmartRouter
         * public class ???ActivitySmartRouter extends BaseActivityRouter implements Router<???Activity>
         */
        TypeSpec smartRouterClass = TypeSpec
            .classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .superclass(RouterType.BASE_ACTIVITY_ROUTER)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    RouterType.ROUTER,
                    TypeVariableName.get(this.annotatedElementSimpleName)
                )
            )
            .addJavadoc("Generated code from SmartRouter. Do not modify !\n\n")
            .addJavadoc("@author CaMnter\n")
            /*
             * private static final SmartRouter REGISTER_INSTANCE = new SmartRouter("");
             * static {
             * -   SmartRouters.register(REGISTER_INSTANCE);
             * }
             */
            .addField(this.staticFieldBuilder(className).build())
            .addStaticBlock(this.staticBlockBuilder().build())
            /*
             * SmartRouter(@NonNull final String host)
             * SmartRouter # register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
             * SmartRouter # setFieldValue(@NonNull final Activity activity)
             * SmartRouter # putValue(final int value)
             */
            .addMethod(this.constructorBuilder().build())
            .addMethod(this.registerMethodBuilder().build())
            .addMethod(this.setFieldValueMethodBuilder().build())
            .addMethods(this.putFieldMethodBuilder())
            .build();

        return JavaFile.builder(this.annotatedElementPackageName, smartRouterClass).build();
    }


    /**
     * private static final SmartRouter REGISTER_INSTANCE = new SmartRouter("");
     *
     * @return FieldSpec.Builder
     */
    private FieldSpec.Builder staticFieldBuilder(String className) {
        final ClassName fieldClassName = ClassName.get(this.getPackageName(), className);
        return
            FieldSpec.builder(fieldClassName, "REGISTER_INSTANCE")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T(\"\")", fieldClassName);
    }


    /**
     * static {
     * -   SmartRouters.register(REGISTER_INSTANCE);
     * }
     *
     * @return CodeBlock.Builder
     */
    private CodeBlock.Builder staticBlockBuilder() {
        return CodeBlock.builder().add("$T.register(REGISTER_INSTANCE);\n", SMART_ROUTERS);
    }


    /**
     * SmartRouter(@NonNull final String host)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder constructorBuilder() {
        return
            MethodSpec.constructorBuilder()
                .addParameter(
                    this.createNonNullParameter(
                        TypeName.get(String.class),
                        "host",
                        Modifier.FINAL
                    )
                )
                .addCode("super(host);\n");
    }


    /**
     * SmartRouter # register(@NonNull final Map<String, Class<? extends Activity>>
     * routerMapping)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder registerMethodBuilder() {

        // public void register(@NonNull final Map<String, Class<? extends Activity>> routerMapping)
        final MethodSpec.Builder registerMethodBuilder = MethodSpec.methodBuilder("register")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(
                this.createNonNullParameter(
                    ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(String.class),
                        ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(RouterType.ANDROID_ACTIVITY)
                        )
                    ),
                    "routerMapping",
                    Modifier.FINAL
                )
            );

        // routerMapping.put($1L, $2L.class)
        for (RouterHostAnnotation routerHostAnnotation : this.routerHostAnnotationList) {
            final String activitySimpleName = routerHostAnnotation.getElement()
                .getSimpleName()
                .toString();
            for (String host : routerHostAnnotation.getHost()) {
                registerMethodBuilder.addCode("routerMapping.put($S, $L.class);\n", host,
                    activitySimpleName);
            }
        }

        return registerMethodBuilder;
    }


    /**
     * SmartRouter # setFieldValue(@NonNull final Activity activity)
     *
     * @return MethodSpec.Builder
     */
    private MethodSpec.Builder setFieldValueMethodBuilder() {

        // public void setFieldValue(@NonNull Activity activity)
        final MethodSpec.Builder setFieldValueMethodBuilder = MethodSpec.methodBuilder(
            "setFieldValue")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(
                this.createNonNullParameter(
                    this.annotatedElementTypeName,
                    "activity",
                    Modifier.FINAL
                )
            );

        // final Intent intent = activity.getIntent()
        if (!this.routerFieldAnnotationList.isEmpty()) {
            setFieldValueMethodBuilder.addCode(
                CodeBlock.of("final $T intent = activity.getIntent();\n", ANDROID_INTENT)
            );
        }
        for (RouterFieldAnnotation routerFieldAnnotation : this.routerFieldAnnotationList) {
            final String fieldTypeString = routerFieldAnnotation.getFieldType().toString();
            final String fieldName = routerFieldAnnotation.getFieldName().toString();
            CodeBlock codeBlock = null;
            switch (fieldTypeString) {
                case CHAR:
                case BOXED_CHAR:
                    codeBlock = CodeBlock.of("intent.getCharExtra($S, (char) 0);\n", fieldName);
                    break;
                case BYTE:
                case BOXED_BYTE:
                    codeBlock = CodeBlock.of("intent.getByteExtra($S, (byte) 0);\n", fieldName);
                    break;
                case SHORT:
                case BOXED_SHORT:
                    codeBlock = CodeBlock.of("intent.getShortExtra($S, (short) 0);\n", fieldName);
                    break;
                case INT:
                case BOXED_INT:
                    codeBlock = CodeBlock.of("intent.getIntExtra($S, 0);\n", fieldName);
                    break;
                case FLOAT:
                case BOXED_FLOAT:
                    codeBlock = CodeBlock.of("intent.getFloatExtra($S, 0f);\n", fieldName);
                    break;
                case DOUBLE:
                case BOXED_DOUBLE:
                    codeBlock = CodeBlock.of("intent.getDoubleExtra($S, 0d);\n", fieldName);
                    break;
                case LONG:
                case BOXED_LONG:
                    codeBlock = CodeBlock.of("intent.getLongExtra($S, 0L);\n", fieldName);
                    break;
                case BOOLEAN:
                case BOXED_BOOLEAN:
                    codeBlock = CodeBlock.of("intent.getBooleanExtra($S, false);\n", fieldName);
                    break;
                case STRING:
                    codeBlock = CodeBlock.of("intent.getStringExtra($S);\n", fieldName);
                    break;
            }
            if (codeBlock == null) {
                continue;
            }
            setFieldValueMethodBuilder.addCode(codeBlock);
        }

        return setFieldValueMethodBuilder;
    }


    /**
     * SmartRouter # putValue(final int value)
     * SmartRouter # putValue(@NonNull final Integer value)
     *
     * @return List<MethodSpec>
     */
    private List<MethodSpec> putFieldMethodBuilder() {

        final List<MethodSpec> putMethods = new ArrayList<>();

        for (RouterFieldAnnotation routerFieldAnnotation : this.routerFieldAnnotationList) {
            final TypeMirror fieldTypeMirror = routerFieldAnnotation.getFieldType();
            final TypeName fieldTypeName = TypeName.get(fieldTypeMirror);
            final String fieldTypeString = fieldTypeMirror.toString();
            final String fieldName = routerFieldAnnotation.getFieldName().toString();

            final String expectName = "put" + fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1);

            final MethodSpec.Builder putMethodBuilder = MethodSpec
                .methodBuilder(expectName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addCode(CodeBlock.of("this.put($S, value);\n", fieldName));

            switch (fieldTypeString) {
                case CHAR:
                case BYTE:
                case SHORT:
                case INT:
                case FLOAT:
                case DOUBLE:
                case LONG:
                case BOOLEAN:
                    putMethodBuilder.addParameter(fieldTypeName, "value", Modifier.FINAL);
                    putMethods.add(putMethodBuilder.build());
                    break;
                case BOXED_CHAR:
                case BOXED_BYTE:
                case BOXED_SHORT:
                case BOXED_INT:
                case BOXED_FLOAT:
                case BOXED_DOUBLE:
                case BOXED_LONG:
                case BOXED_BOOLEAN:
                case STRING:
                    putMethodBuilder.addParameter(
                        this.createNonNullParameter(
                            fieldTypeName,
                            "value",
                            Modifier.FINAL
                        )
                    );
                    putMethods.add(putMethodBuilder.build());
                    break;
            }
        }
        return putMethods;
    }

}