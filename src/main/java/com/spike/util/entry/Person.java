package com.spike.util.entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

import static org.springframework.ldap.odm.annotations.Attribute.Type.BINARY;

@Entry(base = "DC=asia,DC=roche,DC=com", objectClasses = "user")
@Data
public class Person {

    //entry的唯一辨别名，一条完整的DN写法：uid=zhang3,ou=People,dc=163,dc=com
    @Id
    @JsonIgnore // 必写
    private Name dn;

    //对象唯一id
    @Attribute(name = "objectGUID", type = BINARY)
    private String objectGUID;

    private String objectGUIDStr;

    //cn
    @Attribute(name = "cn")
    private String cn;

    //sn
    @Attribute(name = "sn")
    private String sn;

    //distinguishedName
    @Attribute(name = "distinguishedName")
    private String distinguishedName;

    //显示名字
    @Attribute(name = "displayName")
    private String displayName;

    //名称
    @Attribute(name = "name")
    private String name;

    //samAccountName
    @Attribute(name = "sAMAccountName")
    private String sAMAccountName;

    //用户主体名称
    @Attribute(name = "accountExpires")
    private String accountExpires;

    //userPrincipalName
    @Attribute(name = "userPrincipalName")
    private String userPrincipalName;

    //罗氏姓氏
    @Attribute(name = "rocheLegalSurname")
    private String rocheLegalSurname;

    //罗氏名字
    @Attribute(name = "rocheLegalGivenName")
    private String rocheLegalGivenName;

    //罗氏名字
    @Attribute(name = "givenName")
    private String givenName;

    //员工id
    @Attribute(name = "employeeID")
    private String employeeID;

    //邮件
    @Attribute(name = "mail")
    private String mail;

    //手机
    @Attribute(name = "mobile")
    private String mobile;

    //座机
    @Attribute(name = "telephoneNumber")
    private String telephoneNumber;

    public static String getGUID(String[] inArr) {
        String[] strArr = new String[inArr.length];
        strArr[0] = inArr[3];
        strArr[1] = inArr[2];
        strArr[2] = inArr[1];
        strArr[3] = inArr[0];
        strArr[4] = inArr[5];
        strArr[5] = inArr[4];
        strArr[6] = inArr[7];
        strArr[7] = inArr[6];
        strArr[8] = inArr[8];
        strArr[9] = inArr[9];
        strArr[10] = inArr[10];
        strArr[11] = inArr[11];
        strArr[12] = inArr[12];
        strArr[13] = inArr[13];
        strArr[14] = inArr[14];
        strArr[15] = inArr[15];
        StringBuffer guid = new StringBuffer();
        for (int i = 0; i < strArr.length; i++) {
            byte temp = Byte.parseByte(strArr[i]);
            String dblByte = Integer.toHexString(temp & 0xff);
            if (dblByte.length() == 1) {
                guid.append("0");
            }
            guid.append(dblByte);
        }
        return guid.toString();
    }

}
