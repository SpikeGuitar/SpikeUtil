package com.spike.util.Enum;

/**
 * @className: FieldEnum
 * @author: spike.william
 * @date: 2022/11/22 10:15
 **/
public enum FieldEnum {
    MISS_PARAMETER("Missing paging parameter！","缺失分页参数！"),
    SEQUEN_PLAT("sequencingPlatForm","sequencingPlatForm"),
    DATA("data","data"),
    ID("id","id"),
    SCHEME_NO("schemeNo","schemeNo"),
    REMARK_NO("remarkNo","remarkNo"),
    CUR_PAGE("curPage","curPage"),
    APPROVAL("approval","approval"),
    UPDATE(" UPDATE ","UPDATE"),
    WHERE(" WHERE ","UPDATE"),
    SCHEMA_CODE("schemaCode","schemaCode");

    FieldEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;
    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
