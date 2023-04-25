package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.constant.Constant;

import java.text.MessageFormat;

/**
 *
 * @author Jeff Butler
 *
 */
public class ListByPageElementGenerator extends
        AbstractXmlElementGenerator {

    public ListByPageElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        String fqjt = introspectedTable.getExampleType();

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        //XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getListByPageStatementId())); //$NON-NLS-1$

        String parameterType = introspectedTable.getQueryVoType();

        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType));
        answer.addAttribute(new Attribute(
                "resultMap", introspectedTable.getBaseResultMapId()));
        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();

        sb.append("select * from "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("where"); //$NON-NLS-1$
        answer.addElement(dynamicElement);

        for (IntrospectedColumn introspectedColumn : introspectedTable.getColumnsInVO()) {
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null "); //$NON-NLS-1$
            if(introspectedColumn.isStringColumn()) {
                sb.append(MessageFormat.format("and {0}.trim() != '''' ", introspectedColumn.getJavaProperty()));
            }
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append("and " + MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn));
            //sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }
        dynamicElement.addElement(new TextElement(" and " + Constant.ENABLED_FLAG + " = " + Constant.ENABLED_FLAG_Y +" "));
        answer.addElement(new TextElement("order by " + Constant.CREATION_DATE + " desc"));
        if (context.getPlugins()
                .sqlMapUpdateByPrimaryKeySelectiveElementGenerated(answer,
                        introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}

