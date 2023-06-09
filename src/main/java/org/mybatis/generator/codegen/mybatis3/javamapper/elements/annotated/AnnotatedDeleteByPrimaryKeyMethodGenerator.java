/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getEscapedColumnName;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getParameterClause;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import java.util.Iterator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.DeleteByPrimaryKeyMethodGenerator;
import org.mybatis.generator.constant.Constant;

/**
 * 
 * @author Jeff Butler
 */
public class AnnotatedDeleteByPrimaryKeyMethodGenerator extends
        DeleteByPrimaryKeyMethodGenerator {

    public AnnotatedDeleteByPrimaryKeyMethodGenerator(boolean isSimple) {
        super(isSimple);
    }

    @Override
    public void addMapperAnnotations(Method method) {

        /*method.addAnnotation("@Delete({"); //$NON-NLS-1$

        StringBuilder sb = new StringBuilder();
        javaIndent(sb, 1);
        sb.append("\"delete from "); //$NON-NLS-1$
        sb.append(escapeStringForJava(
                introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        sb.append("\","); //$NON-NLS-1$
        method.addAnnotation(sb.toString());

        boolean and = false;
        Iterator<IntrospectedColumn> iter = introspectedTable.getPrimaryKeyColumns().iterator();
        while (iter.hasNext()) {
            sb.setLength(0);
            javaIndent(sb, 1);
            if (and) {
                sb.append("  \"and "); //$NON-NLS-1$
            } else {
                sb.append("\"where "); //$NON-NLS-1$
                and = true;
            }

            IntrospectedColumn introspectedColumn = iter.next();
            sb.append(escapeStringForJava(
                    getEscapedColumnName(introspectedColumn)));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(getParameterClause(introspectedColumn));
            sb.append('\"');
            if (iter.hasNext()) {
                sb.append(',');
            }

            method.addAnnotation(sb.toString());
        }*/

        method.addAnnotation("@Delete({"); //$NON-NLS-1$

        StringBuilder sb = new StringBuilder();
        javaIndent(sb, 1);
        sb.append("\"update "); //$NON-NLS-1$
        sb.append(escapeStringForJava(
                introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        sb.append(" set " + Constant.ENABLED_FLAG + "=" + Constant.ENABLED_FLAG_N +",\","); //$NON-NLS-1$
        /*"last_update_date = #{lastUpdateDate,jdbcType=TIMESTAMP},",
                "last_updated_by = #{lastUpdatedBy,jdbcType=VARCHAR}",
                "where user_app_page_id = #{userAppPageId,jdbcType=VARCHAR}"*/
        method.addAnnotation(sb.toString());
        String[] others = {
                "\" " + Constant.LAST_UPDATE_DATE + " = #{" + Constant.LAST_UPDATE_DATE_FIELD_NAME + ",jdbcType=TIMESTAMP},\",",
                "\"" + Constant.LAST_UPDATED_BY + " = #{" + Constant.LAST_UPDATED_BY_FIELD_NAME + ",jdbcType=VARCHAR},\",",
                "\"" + Constant.LAST_UPDATED_BY_NAME + " = #{" + Constant.LAST_UPDATED_BY_NAME_FIELD_NAME + ",jdbcType=VARCHAR}\","
        };

        for(String other : others) {
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(other);
            method.addAnnotation(sb.toString());
        }

        boolean and = false;
        Iterator<IntrospectedColumn> iter = introspectedTable.getPrimaryKeyColumns().iterator();
        while (iter.hasNext()) {
            sb.setLength(0);
            javaIndent(sb, 1);
            if (and) {
                sb.append("  \"and "); //$NON-NLS-1$
            } else {
                sb.append("\"where "); //$NON-NLS-1$
                and = true;
            }

            IntrospectedColumn introspectedColumn = iter.next();
            sb.append(escapeStringForJava(
                    getEscapedColumnName(introspectedColumn)));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(getParameterClause(introspectedColumn));
            sb.append('\"');
            sb.append(',');
            method.addAnnotation(sb.toString());
        }

        sb.setLength(0);
        javaIndent(sb, 1);
        sb.append("\"and " + Constant.ENABLED_FLAG + " = " + Constant.ENABLED_FLAG_Y +"\"");
        method.addAnnotation(sb.toString());

        method.addAnnotation("})"); //$NON-NLS-1$
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Delete")); //$NON-NLS-1$
    }
}
