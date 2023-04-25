/**
 * Copyright 2006-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.model;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansGetter;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansSetter;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.codegen.mybatis3.Constant;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.BeanUtils;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * @author Jeff Butler
 */
public class BaseRecordGenerator extends AbstractJavaGenerator {

    public BaseRecordGenerator() {
        super();
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString(
                "Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        //加Data注解
        //@Data
        topLevelClass.addAnnotation(Constant.DATA_ANNOTATION);
        topLevelClass.addImportedType(Constant.LOMBOK_DATA);

        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }
        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable.getNonBLOBColumns());

            if (includeBLOBColumns()) {
                addParameterizedConstructor(topLevelClass, introspectedTable.getAllColumns());
            }

            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }

        String rootClass = getRootClass();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(rootClass, warnings)
                    .containsProperty(introspectedColumn)) {
                continue;
            }

            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            if (plugins.modelFieldGenerated(field, topLevelClass,
                    introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
                if(!field.getAnnotations().isEmpty()){
                    topLevelClass.addImportedType(Constant.PRIMARY_KEY_ANO_PAKAGE);
                }
            }

            /*Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (plugins.modelGetterMethodGenerated(method, topLevelClass,
                    introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addMethod(method);
            }

            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass,
                        introspectedColumn, introspectedTable,
                        Plugin.ModelClassType.BASE_RECORD)) {
                    topLevelClass.addMethod(method);
                }
            }*/
        }

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().modelBaseRecordClassGenerated(
                topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        answer.addAll(getVo(topLevelClass));
        answer.addAll(getQueryVo(topLevelClass));
        return answer;
    }

    public List<String> getAbandonFields() {
        return introspectedTable.getAbandonFieldsForVO();
    }

    private List<CompilationUnit> getVo(TopLevelClass modelClass) {

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        String prop = context.getJavaModelGeneratorConfiguration()
                .getProperty(PropertyRegistry.VO_PACKAGE);
        if (StringUtility.stringHasValue(prop)) {
            TopLevelClass voClass = BeanUtils.deepClone(modelClass);
            String voName = prop + "."
                    + modelClass.getType().getShortName()
                    + "VO";
            voClass.setType(new FullyQualifiedJavaType(voName));
            //筛选字段和方法
            abandonFieldsAndMethods(voClass);

            //加ApiModel注解
            addApiModelAnnotation(voClass);
            //为字段加ApiModelProperty
            addAnnotationOnField(voClass);

            //注册
            introspectedTable.setVoType(voName);
            answer.add(voClass);

        }
        return answer;
    }

    private void abandonFieldsAndMethods(TopLevelClass voClass) {
        List<Field> fields = voClass.getFields();
        Iterator<Field> fieldIterator = fields.iterator();
        while (fieldIterator.hasNext()) {
            String currentName = fieldIterator.next().getName();
            for (String fieldName : getAbandonFields()) {
                if (fieldName.equalsIgnoreCase(currentName)) {
                    fieldIterator.remove();
                    break;
                }
            }
        }
        //去除getter和setter方法
        List<Method> methods = voClass.getMethods();
        Iterator<Method> methodIterator = methods.iterator();
        while (methodIterator.hasNext()) {
            String currentName = methodIterator.next().getName();
            if (currentName.length() < 4) {
                continue;
            }
            String relevantFieldName = currentName.substring(3);
            for (String fieldName : getAbandonFields()) {
                if (fieldName.equalsIgnoreCase(relevantFieldName)) {
                    methodIterator.remove();
                    break;
                }
            }
        }
    }


    private List<CompilationUnit> getQueryVo(TopLevelClass modelClass) {
        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        String prop = context.getJavaModelGeneratorConfiguration()
                .getProperty(PropertyRegistry.QUERY_VOPACKAGE);
        if (StringUtility.stringHasValue(prop)) {
            TopLevelClass queryVoClass = BeanUtils.deepClone(modelClass);
            String queryVoName = prop + "."
                    + modelClass.getType().getShortName()
                    + "QueryVO";
            queryVoClass.setType(new FullyQualifiedJavaType(queryVoName));

            //筛选字段
            //筛选字段和方法
            abandonFieldsAndMethods(queryVoClass);

            //加ApiModel注解
            addApiModelAnnotation(queryVoClass);
            //为字段加ApiModelProperty
            addAnnotationOnField(queryVoClass);
            String superClassName = context.getJavaModelGeneratorConfiguration()
                    .getProperty(PropertyRegistry.QUERY_VOSUPER_CLASS);
            if (StringUtility.stringHasValue(superClassName)) {
                queryVoClass.setSuperClass(superClassName);
                queryVoClass.addImportedType(superClassName);
            }
            introspectedTable.setQueryVoType(queryVoName);
            answer.add(queryVoClass);
        }
        return answer;
    }

    private void addAnnotationOnField(TopLevelClass targetClass) {
        List<Field> fields = targetClass.getFields();
        List<Field> fieldAns = fields.stream().filter(f->f.getOriginConment() != null).collect(Collectors.toList());
        fieldAns.forEach(
                field -> field.addAnnotation(MessageFormat.format(
                         Constant.API_MODEL_PROPERTY + "(value=\"{0}\")", field.getOriginConment()))
        );
        if(!fieldAns.isEmpty()) {
            targetClass.addImportedType(Constant.API_MODEL_PROPERTYL_FULL_NAME);
        }
    }

    private void addApiModelAnnotation(TopLevelClass targetClass) {
        targetClass.addAnnotation(Constant.API_MODEL);
        targetClass.addImportedType(Constant.API_MODEL_FULL_NAME);
    }


    private FullyQualifiedJavaType getSuperClass() {
        FullyQualifiedJavaType superClass;
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            superClass = new FullyQualifiedJavaType(introspectedTable
                    .getPrimaryKeyType());
        } else {
            String rootClass = getRootClass();
            if (rootClass != null) {
                superClass = new FullyQualifiedJavaType(rootClass);
            } else {
                superClass = null;
            }
        }

        return superClass;
    }

    private boolean includePrimaryKeyColumns() {
        return !introspectedTable.getRules().generatePrimaryKeyClass()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    private boolean includeBLOBColumns() {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass, List<IntrospectedColumn> constructorColumns) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.setName(topLevelClass.getType().getShortName());
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
                    introspectedColumn.getJavaProperty()));
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
        }

        StringBuilder sb = new StringBuilder();
        List<String> superColumns = new LinkedList<String>();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
                superColumns.add(introspectedColumn.getActualColumnName());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            if (!superColumns.contains(introspectedColumn.getActualColumnName())) {
                sb.setLength(0);
                sb.append("this."); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" = "); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(';');
                method.addBodyLine(sb.toString());
            }
        }

        topLevelClass.addMethod(method);
    }

    private List<IntrospectedColumn> getColumnsInThisClass() {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns()) {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable
                        .getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }
}
