package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.BeanUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author by 郑东(10259829) on 2019/11/18 16:21
 */
public class ListByPageMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public ListByPageMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        String queryVo = introspectedTable.getQueryVoType();
        if (queryVo == null || queryVo.length() < 1) {
            System.out.println("no queryvo, not generate listByPage method");
            return;
        }
        Method method = new Method();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType
                .getNewListInstance();
        FullyQualifiedJavaType listType = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());
        importedTypes.add(listType);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);

        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(introspectedTable.getListByPageStatementId());
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(introspectedTable.getQueryVoType());

        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, BeanUtils.firstToLower(parameterType.getShortName()))); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        addMapperAnnotations(method);

        addExtraImports(interfaze);
        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);

    }

    public void addMapperAnnotations(Method method) {
    }

    public void addExtraImports(Interface interfaze) {
    }
}
