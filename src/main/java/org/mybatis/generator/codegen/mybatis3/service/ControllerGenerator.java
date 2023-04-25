package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.Constant;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.internal.util.BeanUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.codegen.mybatis3.Constant.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author by 郑东(10259829) on 2019/11/18 9:24
 */
public class ControllerGenerator extends AbstractJavaGenerator {
    private ServiceGenerator serviceGenerator;

    public ControllerGenerator(ServiceGenerator serviceGenerator) {
        super();
        this.serviceGenerator = serviceGenerator;
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        Interface service = getServiceInterface();
        if (service != null) {
            answer.addAll(getController(service));
        }
        //
        return answer;
    }

    private Interface getServiceInterface() {
        List<CompilationUnit> compilationUnits = serviceGenerator.getCompilationUnits();
        for (CompilationUnit unit : compilationUnits) {
            if (unit instanceof Interface) {
                return (Interface) unit;
            }
        }
        return null;
    }

    private List<CompilationUnit> getController(Interface interFace) {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();


        String name = introspectedTable.getBaseRecordShortName();
        //service实现生成强命名
        String fullServiceName = getJavaControllerGeneratorConfiguration().getTargetPackage() + "." + name + "Controller";

        System.out.println(fullServiceName);
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(fullServiceName);

        TopLevelClass topLevelClass = new TopLevelClass(type);

        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        //加Slf4j注解
        topLevelClass.addAnnotation(Constant.SLF4J_ANNOTATION);
        topLevelClass.addImportedType(Constant.SLF4J);

        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }

        //加注解
        /*@Api(value = "app信息", tags = {"app信息"})
                @RestController
                @RequestMapping(value = "/app")*/
        topLevelClass.addAnnotation("@Api(value = \"" + topLevelClass.getType().getShortName()
                + "\", tags = {\"" + topLevelClass.getType().getShortName() + "\"})");
        topLevelClass.addImportedType("io.swagger.annotations.*");
        topLevelClass.addAnnotation("@RestController");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.RestController");
        topLevelClass.addAnnotation(MessageFormat.format("@RequestMapping(value = \"/{0}\")",
                BeanUtils.removeLastStr(BeanUtils.firstToLower(topLevelClass.getType().getShortName()), "Controller") ));
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestMapping");


        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

        //注入mapper并返回mapper Name
        String interFaceName = inject(topLevelClass, interFace);

        //实现接口方法
        implMethodsFromInterface(topLevelClass, interFace, interFaceName);


        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass);

            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }


        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass,
                introspectedTable)) {
            answer.add(topLevelClass);
        }
        return answer;
    }

    private JavaServiceGeneratorConfiguration getJavaServiceGeneratorConfiguration() {
        return introspectedTable.getContext().getJavaServiceGeneratorConfiguration();
    }

    private JavaControllerGeneratorConfiguration getJavaControllerGeneratorConfiguration() {
        return introspectedTable.getContext().getJavaControllerGeneratorConfiguration();
    }


    private String firstToLower(String name) {
        return name.substring(0, 1).toLowerCase() +
                (name.length() > 1 ? name.substring(1) : "");
    }

    private FullyQualifiedJavaType getSuperClass() {
        FullyQualifiedJavaType superClass;
        String rootClass = getRootClass();
        if (rootClass != null) {
            superClass = new FullyQualifiedJavaType(rootClass);
        } else {
            superClass = null;
        }

        return superClass;
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.setName(topLevelClass.getType().getShortName());
        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        List<IntrospectedColumn> constructorColumns = introspectedTable
                .getAllColumns();

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn
                    .getFullyQualifiedJavaType(), introspectedColumn
                    .getJavaProperty()));
        }

        StringBuilder sb = new StringBuilder();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            sb.setLength(0);
            sb.append("this."); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" = "); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(';');
            method.addBodyLine(sb.toString());
        }

        topLevelClass.addMethod(method);
    }

    private String inject(TopLevelClass topLevelClass, Interface injectTarget) {
        String name = firstToLower(injectTarget.getType().getShortName());
        topLevelClass.addImportedType(injectTarget.getType());
        Field field = new Field(name, injectTarget.getType());
        field.setVisibility(JavaVisibility.PRIVATE);
        //加注解
        field.addAnnotation("@Autowired");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));
        topLevelClass.addField(field);

        return name;
    }

    private void implMethodsFromInterface(TopLevelClass topLevelClass, Interface interFace, String serviceName) {

        topLevelClass.addImportedType(RETURN_INCLUDE_STATIC_SUCCESS_CLASS_NAME);
        //实现接口的方法
        List<Method> methods = interFace.getMethods();
        for (Method method : methods) {
            Method methodCopy = new Method(method);
            //保持调用位置不动
            addMethodAnnotations(methodCopy);
            if(methodCopy.getName().contains("Selective")) {
                continue;
            }
            if (serviceName != null) {
                String splitor = ", ";
                StringBuilder stringBuilder = new StringBuilder("return " + RETURN_INCLUDE_STATIC_SUCCESS_CLASS_SHORT_NAME + ".success(" + serviceName + "." + method.getName() + "(");
                for (Parameter parameter : methodCopy.getParameters()) {
                    stringBuilder.append(parameter.getName()).append(splitor);
                }
                if (stringBuilder.toString().endsWith(splitor)) {
                    stringBuilder.setLength(stringBuilder.length() - splitor.length());
                }
                stringBuilder.append("));");
                methodCopy.addBodyLine(stringBuilder.toString());
                //重新设置返回类型
                String returnTypeStr = Constant.RETURN_TYPE_CLASS_STR;
                FullyQualifiedJavaType javaType = new FullyQualifiedJavaType(returnTypeStr);
                if(methodCopy.getReturnType().isPrimitive()) {
                    javaType.addTypeArgument(FullyQualifiedJavaType.getObjectInstance());
                } else {
                    javaType.addTypeArgument(methodCopy.getReturnType());
                }

                topLevelClass.addImportedTypes(methodCopy.getReturnType().getImportList());
                topLevelClass.addImportedType(returnTypeStr);
                methodCopy.setReturnType(javaType);
                //为入参类型导入
                for (Parameter parameter : methodCopy.getParameters()) {
                    topLevelClass.addImportedTypes(parameter.getType().getImportList());
                }
            } else {
                //为了生成空方法体，自行补全类型
                methodCopy.addBodyLine("");

            }
            topLevelClass.addImportedType("org.springframework.web.bind.annotation.*");
            topLevelClass.addMethod(methodCopy);
        }

    }

    private void addMethodAnnotations(Method methodCopy) {
        /*@ApiOperation("插入app对象")
        @PostMapping*/
        String objName = introspectedTable.getBaseRecordShortName();
        String name = methodCopy.getName();
        if (name.contains("select") || name.contains("list")) {
            String ant = "@ApiOperation(\"查询{0}对象\")";
            if (name.contains("ByPage")) {
                ant = "@ApiOperation(\"查询{0}对象-支持分页\")";
                methodCopy.addAnnotation("@GetMapping(\"/page\")");
            }else if (name.contains(LIST_BY)) {
                ant = "@ApiOperation(\"查询{0}对象-不支持分页\")";
                methodCopy.addAnnotation("@GetMapping(\"/"+ LIST_BY +"\")");
            } else{
                methodCopy.addAnnotation("@GetMapping");
            }
            methodCopy.addAnnotation(MessageFormat.format(ant, objName));

            for (Parameter parameter : methodCopy.getParameters()) {
                //库对象用RequestParam注解
                if(parameter.getType().getPackageName().startsWith("java")) {
                    parameter.addAnnotation("@RequestParam");
                } else {
                    parameter.addAnnotation("@ModelAttribute");
                }

            }
        } else if (name.contains("insert")) {
            String ant = "@ApiOperation(\"插入{0}对象\")";
            methodCopy.addAnnotation(MessageFormat.format(ant, objName));
            methodCopy.addAnnotation("@PostMapping");
            for (Parameter parameter : methodCopy.getParameters()) {
                parameter.addAnnotation("@RequestBody");
            }
        } else if (name.contains("update")) {
            String ant = "@ApiOperation(\"编辑{0}对象\")";
            methodCopy.addAnnotation(MessageFormat.format(ant, objName));
            methodCopy.addAnnotation("@PutMapping");
            for (Parameter parameter : methodCopy.getParameters()) {
                parameter.addAnnotation("@RequestBody");
            }
        } else if (name.contains("delete")) {
            String ant = "@ApiOperation(\"删除{0}对象\")";
            methodCopy.addAnnotation(MessageFormat.format(ant, objName));
            methodCopy.addAnnotation("@DeleteMapping");
            for (Parameter parameter : methodCopy.getParameters()) {
                parameter.addAnnotation("@RequestBody");
            }
        }
    }

}
