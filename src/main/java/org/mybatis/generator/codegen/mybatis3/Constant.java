package org.mybatis.generator.codegen.mybatis3;

/**
 * @author by 郑东(10259829) on 2019/11/19 19:21
 */
public class Constant {
    public static final String CMP_PRFEX = "com.pingan.pahm" ;
    public static final String BEAN_UTILS = CMP_PRFEX + ".util.BeanUtils";
    public static final String RETURN_TYPE_CLASS_STR = CMP_PRFEX + ".global.data.Result";
    public static final String RETURN_INCLUDE_STATIC_SUCCESS_CLASS_NAME = CMP_PRFEX + ".global.data.Result";

    public static final String BY_PAGE = "ByPage";
    public static final String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
    public static final String AUTOWIRED_ANNOTATION = "@Autowired";
    public static final String SERVICE = "org.springframework.stereotype.Service";
    public static final String SERVICE_ANNOTATION = "@Service";
    public static final String LOMBOK_DATA = "lombok.Data";
    public static final String DATA_ANNOTATION = "@Data";
    public static final String SLF4J_ANNOTATION = "@Slf4j";
    public static final String SLF4J = "lombok.extern.slf4j.Slf4j";
    public static final String PRIMARY_KEY_ANO_PAKAGE = "com.pingan.pahm.annotation.*";
    public static final String PRIMARY_KEY_ANNOTATION = "@PrimaryKey";


    public static final String RETURN_INCLUDE_STATIC_SUCCESS_CLASS_SHORT_NAME = "Result";

    public static final String LIST_BY = "listBy";


    public static final String PAGE_PARAM_CLASS_SHORT_NAME = "PageParam";
    public static final String LIST_BY_PAGE = "listByPage";

    public static final String LEFT_BRACKET = "(";

    public static final String API_MODEL = "@ApiModel";
    public static final String API_MODEL_FULL_NAME = "io.swagger.annotations.ApiModel";
    public static final String API_MODEL_PROPERTY = "@ApiModelProperty";
    public static final String API_MODEL_PROPERTYL_FULL_NAME = "io.swagger.annotations.ApiModelProperty";
}
