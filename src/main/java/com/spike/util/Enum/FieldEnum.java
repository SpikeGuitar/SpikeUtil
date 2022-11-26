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
    PATH("path","文件地址"),
    DATE_FORMAT("[0-9]{4}[-][0-9]{2}[-][0-9]{2}","日期格式正则表达式"),
    SPECIAL_FORMAT("([0][.][0-9]+|[1-9][0-9]*|[1-9][0-9]*[.][0-9]*)\\*([0][.][0-9]+|[1-9][0-9]*|[1-9][0-9]*[.][0-9]*)\\*([0][.][0-9]+|[1-9][0-9]*|[1-9][0-9]*[.][0-9]*)","特殊格式正则表达式XX*XX*XX格式"),
    IMG_PATH("imagePath","图片地址"),
    HTTP("http://","http请求头"),
    QR_CODE_MSG("QRCodeMsgName","二维码显示信息"),
    QR_CODE_NAME("QRCodeName","二维码名称"),
    URL("URL","地址"),
    DIR("DIR","目录"),
    GET_2F("%2F","Get请求 /"),
    GET_3F("%3F","Get请求 ? "),
    GET_3D("%3D","Get请求 = "),
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
