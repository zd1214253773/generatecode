package org.mybatis.generator.constant;

import java.util.Arrays;
import java.util.List;

public class Constant {

    public static final String ENABLED_FLAG = "valid";
    public static final String ENABLED_FLAG_Y = "'1'";
    public static final String ENABLED_FLAG_N = "'0'";

    public static final String CREATION_DATE = "create_time";
    public static final String CREATED_BY = "oper_code";
    public static final String CREATED_BY_NAME = "oper_name";
    /**
     * 更新的字段
     */
    public static final String LAST_UPDATED_BY = "modifyer_code";
    public static final String LAST_UPDATED_BY_NAME = "modifyer_name";
    public static final String LAST_UPDATE_DATE = "update_time";

    public static final String CREATION_DATE_FIELD_NAME = "createTime";
    public static final String CREATED_BY_FIELD_NAME = "operCode";
    public static final String CREATED_BY_NAME_FIELD_NAME = "operName";

    public static final String LAST_UPDATE_DATE_FIELD_NAME = "updateTime";
    public static final String LAST_UPDATED_BY_FIELD_NAME = "modifyerCode";
    public static final String LAST_UPDATED_BY_NAME_FIELD_NAME = "modifyerName";

    public static List<String> FIELDS = Arrays.asList(CREATION_DATE_FIELD_NAME, CREATED_BY_FIELD_NAME, CREATED_BY_NAME_FIELD_NAME,
            LAST_UPDATE_DATE_FIELD_NAME, LAST_UPDATED_BY_FIELD_NAME, LAST_UPDATED_BY_NAME_FIELD_NAME, ENABLED_FLAG);
}
