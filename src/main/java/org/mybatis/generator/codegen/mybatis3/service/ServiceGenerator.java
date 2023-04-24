package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.Constant;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.internal.util.BeanUtils;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author by 郑东(10259829) on 2019/11/15 17:21
 */
public class ServiceGenerator extends AbstractJavaGenerator {

    public static final String SPLITOR = ", ";
    private AbstractJavaClientGenerator javaClientGenerator;

    public ServiceGenerator(AbstractJavaClientGenerator javaClientGenerator) {
        super();
        this.javaClientGenerator = javaClientGenerator;
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        Interface interFace = getServiceInterFace();
        answer.add(interFace);
        answer.addAll(getServiceImpl(interFace));
        return answer;
    }

    private List<CompilationUnit> getServiceImpl(Interface interFace) {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();


        String name = introspectedTable.getBaseRecordType().substring(introspectedTable.getBaseRecordType().lastIndexOf(".") + 1);
        //service实现生成强命名
        String fullServiceName = getJavaServiceGeneratorConfiguration().getImplementationPackage() + "." + name + "ServiceImpl";

        System.out.println(fullServiceName);
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(fullServiceName);

        TopLevelClass topLevelClass = new TopLevelClass(type);
        //加Slf4j注解
        topLevelClass.addAnnotation(Constant.SLF4J_ANNOTATION);
        topLevelClass.addImportedType(Constant.SLF4J);

        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);

        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }

        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);
        //注入mapper并返回mapper Name
        String mapperFieldName = injectMapper(topLevelClass);

        //实现接口方法
        implMethodsFromInterface(topLevelClass, interFace, mapperFieldName);

        //导入model dao 对象
        FullyQualifiedJavaType baseRecordType = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());
        topLevelClass.addImportedType(baseRecordType);

        //为service实现类加spring注解
        topLevelClass.addAnnotation(Constant.SERVICE_ANNOTATION);
        topLevelClass.addImportedType(new FullyQualifiedJavaType(Constant.SERVICE));

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

    private Interface getServiceInterFace() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();


        String name = introspectedTable.getBaseRecordType().substring(introspectedTable.getBaseRecordType().lastIndexOf(".") + 1);
        String fullServiceName = getJavaServiceGeneratorConfiguration().getTargetPackage() + "." + name + "Service";
        System.out.println(fullServiceName);
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(fullServiceName);

        //TopLevelClass topLevelClass = new TopLevelClass(type);
        Interface serviceInterface = new Interface(type);
        serviceInterface.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(serviceInterface);
        //父接口
        FullyQualifiedJavaType superInterFace = getSuperClass();
        if (superInterFace != null) {
            serviceInterface.addImportedType(superInterFace);
            serviceInterface.addSuperInterface(superInterFace);
        }
        fillServiceMethods(serviceInterface);
        return serviceInterface;
    }

    private JavaServiceGeneratorConfiguration getJavaServiceGeneratorConfiguration() {
        return context.getJavaServiceGeneratorConfiguration();
    }

    private void fillServiceMethods(Interface serviceInterface) {
        //根据mapper包含的方法来声明service接口里的方法、
        CompilationUnit mapperUnit = javaClientGenerator.getCompilationUnits().get(0);
        if (mapperUnit instanceof Interface) {
            Interface mapperInterface = (Interface) mapperUnit;
            List<Method> methods = mapperInterface.getMethods();
            FullyQualifiedJavaType baseRecordType = new FullyQualifiedJavaType(
                    introspectedTable.getBaseRecordType());
            for (Method method : methods) {
                Method methodCopy = new Method(method);
                methodCopy.getAnnotations().clear();
                if (!methodCopy.getParameters().isEmpty()) {
                    Parameter parameter = methodCopy.getParameters().get(0);
                    //假如第一个参数是domain 参数名record->domain名驼峰形式
                    if (parameter.getType().equals(baseRecordType)) {
                        methodCopy.getParameters().remove(parameter);
                        String shortName = parameter.getType().getShortName();
                        if(isUseProgressive()) {
                            //将vo作为参数
                            FullyQualifiedJavaType voJavaType = new FullyQualifiedJavaType(introspectedTable.getVoType());
                            String shortName2 = BeanUtils.firstToLower(voJavaType.getShortName());
                            methodCopy.getParameters().add(0, new Parameter(voJavaType, shortName2));
                        }else {
                            methodCopy.getParameters().add(0, new Parameter(parameter.getType(),
                                    BeanUtils.firstToLower(shortName)));
                        }

                    }

                    //假如返回类型是baseRecord 或者 baseRecord -> vo or vo list
                    boolean shouldConvert =  isTypeOrListType(introspectedTable.getBaseRecordType(), methodCopy.getReturnType());
                    if(shouldConvert) {
                        FullyQualifiedJavaType voType = new FullyQualifiedJavaType(introspectedTable.getVoType());
                       if(methodCopy.getReturnType().equals(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()))) {
                           methodCopy.setReturnType(voType);
                        } else {
                           FullyQualifiedJavaType listVoType = FullyQualifiedJavaType.getNewListInstance();
                           listVoType.addTypeArgument(voType);
                           methodCopy.setReturnType(listVoType);
                       }
                    }

                    //参数依赖导入
                    for (Parameter param : methodCopy.getParameters()) {
                        serviceInterface.addImportedTypes(param.getType().getImportList());
                    }
                }
                //导入返回参数类型
                serviceInterface.addImportedTypes(methodCopy.getReturnType().getImportList());
                serviceInterface.addMethod(methodCopy);
            }
            serviceInterface.addImportedType(baseRecordType);
        } else {
            System.out.println("mapperUnit:::" + mapperUnit);
        }
    }

    private boolean isUseProgressive() {
       return introspectedTable.isProgressive() && StringUtility.stringHasValue(introspectedTable.getVoType());
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

    private String injectMapper(TopLevelClass topLevelClass) {
        String mapperFieldName = null;

        //注入mapper
        if (!javaClientGenerator.getCompilationUnits().isEmpty()) {
            //其实也就一个对象
            for (CompilationUnit unit : javaClientGenerator.getCompilationUnits()) {
                topLevelClass.addImportedType(unit.getType());
                mapperFieldName = firstToLower(unit.getType().getShortName());
                Field mapperFiled = new Field(mapperFieldName, unit.getType());
                mapperFiled.setVisibility(JavaVisibility.PRIVATE);
                //加注解
                mapperFiled.addAnnotation(Constant.AUTOWIRED_ANNOTATION);
                topLevelClass.addImportedType(new FullyQualifiedJavaType(Constant.AUTOWIRED));

                topLevelClass.addField(mapperFiled);
            }
        }
        return mapperFieldName;
    }

    private void implMethodsFromInterface(TopLevelClass topLevelClass, Interface interFace, String mapperFieldName) {
        //实现接口
        topLevelClass.addImportedType(interFace.getType());
        topLevelClass.addSuperInterface(interFace.getType());
        //实现接口的方法
        List<Method> methods = interFace.getMethods();

        for (Method method : methods) {
            Method methodCopy = new Method(method);
            if (mapperFieldName != null) {
                StringBuilder stringBuilder = new StringBuilder("return ");
                if(isUseProgressive()) {
                    //businessObjectLayoutMapper.insert(BeanUtils.copy(businessObjectLayoutVO, BusinessObjectLayout.class))
                    //处理返回类型
                    boolean voTypeOrListVoType = isTypeOrListType(introspectedTable.getVoType(), methodCopy.getReturnType());

                    String invokeMapperMethodStr = getInvokeMapperMethodStr(topLevelClass, mapperFieldName, methodCopy);


                    if(voTypeOrListVoType) {
                        stringBuilder.append("BeanUtils.copy(");
                        stringBuilder.append(invokeMapperMethodStr);
                        FullyQualifiedJavaType javaVoType = new FullyQualifiedJavaType(introspectedTable.getVoType());
                        stringBuilder.append(", " + javaVoType.getShortName() + ".class");
                        stringBuilder.append(")");
                        topLevelClass.addImportedType(introspectedTable.getVoType());
                    } else {
                        stringBuilder.append(invokeMapperMethodStr);
                    }
                    stringBuilder.append(";");

                } else {
                    stringBuilder.append(mapperFieldName + "." + methodCopy.getName() + "(");
                    for (Parameter parameter : methodCopy.getParameters()) {
                        stringBuilder.append(parameter.getName()).append(SPLITOR);
                        topLevelClass.addImportedTypes(parameter.getType().getImportList());
                    }
                    trimSplitorEndStr(stringBuilder);
                    stringBuilder.append(");");
                }
                methodCopy.addBodyLine(stringBuilder.toString());

                if (methodCopy.getName().contains(Constant.BY_PAGE)) {
                    methodCopy.addJavaDocLine("//TODO");
                }

            } else {
                //为了生成空方法体，自行补全类型
                methodCopy.addBodyLine("");
            }
            methodCopy.addAnnotation("@Override");
            //导入返回参数类型
            topLevelClass.addImportedTypes(methodCopy.getReturnType().getImportList());
            topLevelClass.addMethod(methodCopy);
        }

    }

    //targetType 是否是 sourceType类型或者list<sourceType>类型
    private boolean isTypeOrListType(FullyQualifiedJavaType sourceType, FullyQualifiedJavaType targetType) {

        if(targetType.equals(sourceType)) {
            return true;
        }
        FullyQualifiedJavaType listVoType = FullyQualifiedJavaType.getNewListInstance();
        listVoType.addTypeArgument(sourceType);
        if(targetType.equals(listVoType)) {
            return true;
        }
        return false;
    }

    private boolean isTypeOrListType(String sourceType, FullyQualifiedJavaType targetType) {
        return isTypeOrListType(new FullyQualifiedJavaType(sourceType), targetType);
    }

    private void trimSplitorEndStr(StringBuilder stringBuilder) {
        if (stringBuilder.toString().endsWith(SPLITOR)) {
            stringBuilder.setLength(stringBuilder.length() - SPLITOR.length());
        }
    }

    private String getInvokeMapperMethodStr(TopLevelClass topLevelClass, String mapperFieldName, Method methodCopy) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mapperFieldName + "." + methodCopy.getName() + "(");
        for (Parameter parameter : methodCopy.getParameters()) {
            //BeanUtils.copy(businessObjectLayoutVO
            if(parameter.getType().getFullyQualifiedName().equals(introspectedTable.getVoType())) {
                stringBuilder.append("BeanUtils.copy(").append(parameter.getName())
                        .append(SPLITOR).append(introspectedTable.getBaseRecordShortName())
                        .append(".class)").append(SPLITOR);
                topLevelClass.addImportedType(introspectedTable.getBaseRecordType());
                topLevelClass.addImportedType(Constant.BEAN_UTILS);
            }else {
                stringBuilder.append(parameter.getName()).append(SPLITOR);
            }
            topLevelClass.addImportedTypes(parameter.getType().getImportList());
        }
        trimSplitorEndStr(stringBuilder);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

}