package com.spike.util.Service.Impl;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ExcelUtil;
import com.spike.util.entry.Person;
import com.spike.util.ldapConfig.SsldapContextSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.spike.util.UtilClass.ExcelUtil.batchCheckRegular;
import static com.spike.util.UtilClass.ExcelUtil.checkEnum;
import static com.spike.util.UtilClass.ExcelUtil.checkField;
import static com.spike.util.UtilClass.ExcelUtil.updateExcel;


@Service
@Slf4j
public class UtilServiceImpl implements UtilService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    private static String ANCHOR_POINT = "第.*行导入校验错误,错误信息为：";

    @Value("${spring.lvBaseData.driver-class-name}")
    String driverClass;
    @Value("${spring.lvBaseData.url}")
    String url;
    @Value("${spring.lvBaseData.username}")
    String userName;
    @Value("${spring.lvBaseData.password}")
    String passWord;

    @Value("${spring.mail.username}")
    private String FROM;

    @Value("${spring.ldap.urls:}")
    private String LDAP_URLS;

    @Value("${spring.ldap.username:}")
    private String LDAP_USER_NAME;

    @Value("${spring.ldap.password:}")
    private String LDAP_PASSWORD;

    @Value("${spring.ldap.base:}")
    private String LDAP_BASE;

    @Resource
    private JavaMailSender javaMailSender;

    //Positive integer regular expression
    public static final String SCHEME_NO = "schemeNo";

    public static final String REMARK_NO = "remarkNo";

    //check integer
    public static final String POSITIVE_INT = "[1-9][0-9]*([.]0)?";

    public static final String BIG_THAN_ZERO = "^(?!(0[0-9]{0,}$))[0-9]{1,}[.]{0,}[0-9]{0,}$";

    //Integer greater than or equal to 0
    public static final String INTEGER_BIG_THAN_ZERO = "[0-9]*([.]0)?";

    public void exportExcel(String execlTempleName, Map<String, Object> map, HttpServletResponse response) throws Exception {
        ExcelUtil.ExcelTemplate excelTemplate = ExcelUtil.ExcelTemplate.init(execlTempleName + ".xlsx");
        Workbook workbook = excelTemplate.getWorkbook();
        Sheet sheetAt = workbook.getSheetAt(0);
        //获取动态表头
        if (map.get("tableName") == null || !(map.get("tableName") instanceof List)) {
            log.info("动态标题格式异常，导出失败！");
            return;
        }
        //动态表头
        List<String> tableNames = (List<String>) map.get("tableName");
        //动态表编码
        List<String> tableCode = (List<String>) map.get("tableCode");
        //设置表头
        Row tempRow = sheetAt.createRow(0);
        int col = 0;
        for (String tableName : tableNames) {
            tempRow.createCell(col++).setCellValue(tableName);
        }
        //填充表内容
        int rowValue = 1;
        //创建数据行
        if (map.get("data") == null || !(map.get("data") instanceof List)) {
            log.info("数据格式异常，导出失败！");
            return;
        }
        List<Map> dataList = (List<Map>) map.get("data");
        Row row = sheetAt.createRow(rowValue);
        for (Map dataMap : dataList) {
            row = sheetAt.createRow(rowValue);
            col = 0;
            for (String fileName : tableCode) {
                String value = dataMap.get(fileName) != null ? dataMap.get(fileName).toString() : "";
                if(!value.isEmpty()&& Pattern.matches("[-]?[0-9]*[.]?[0-9]*",value)){
                    row.createCell(col).setCellType(CellType.NUMERIC);
                    double dobValue= Double.valueOf(value);
                    row.createCell(col++).setCellValue(dobValue);
                }else {
                    row.createCell(col).setCellType(CellType.STRING);
                    row.createCell(col++).setCellValue(value);
                }
            }
            rowValue = rowValue + 1;
        }
        String fileName = "DFlow" + System.currentTimeMillis();
        excelTemplate.export(response, fileName);
        return;
    }

    /**
     * 发送邮件接口
     */
    @Override
    public String sendEmail(String emailSubject, String emailText, List<String> addressee) {
        if (emailSubject.isEmpty() || emailText.isEmpty() || addressee.size() < 1) {
            return "发送邮件失败！请检测入参";
        }
        String[] to = addressee.toArray(new String[]{});
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(FROM);//发送者邮箱地址
            messageHelper.setTo(to);//收件人邮箱地址
            messageHelper.setSubject(emailSubject);//邮件主题
            messageHelper.setText(emailText, true);//邮件内容
            messageHelper.setSentDate(new Date());
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.info("邮件发送失败", e);
            new RuntimeException("邮件发送失败", e);
        }
        return "发送邮件成功！";
    }

    @Override
    public void generateAttachmentPackage(Map<String, Object> map, HttpServletResponse response) throws Exception {
        log.info("Start check Params!");
        if (map.get("attachList") != null && map.get("attachList") instanceof List) {
            List<Map<String, Object>> attachList = (List<Map<String, Object>>) map.get("attachList");
            log.info("Start Pack Accessories!");
            // "stream" put 需要放入压缩包的文件 比特数组
            for (Map<String, Object> attach : attachList) {
                //文件输入流
                InputStream inputStream = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };
                ByteArrayOutputStream out = reader(inputStream);
                attach.put("stream", out.toByteArray());
            }
            //生成几级目录
            String mkdir = (String) map.get("mkdir");
            zipFiles(attachList, response, mkdir);
            log.info("End Pack Accessories!");
        } else {
            log.info("Params Erro!");
        }
    }

    public List<Person> findAll() {
        List<Person> resultList = ldapTemplate().findAll(Person.class);
        for (Person person : resultList) {
            String[] obj = person.getObjectGUID().split(",");
            log.info("开始转换 objectGuid :{}", obj);
            String guidStr = Person.getGUID(obj);
            log.info("objectGuid :{}", guidStr);
            person.setObjectGUIDStr(guidStr);
            log.info("objectGuid 转换结束");
        }
        log.info("ldap 查询成功!");
        return resultList;
    }

    public LdapTemplate ldapTemplate() {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource());
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }

    public ContextSource contextSource() {
        // ldaps使用自定义的支持SSL的Context配置
        LdapContextSource contextSource = new SsldapContextSource();
        contextSource.setUserDn(LDAP_USER_NAME);
        contextSource.setPassword(LDAP_PASSWORD);
        contextSource.setUrl(LDAP_URLS);
        contextSource.setBase(LDAP_BASE);
        contextSource.setAnonymousReadOnly(false);
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();
        final Map<String, Object> envProps = new HashMap<>();
        envProps.put("java.naming.ldap.attributes.binary", "objectGUID");
        contextSource.setBaseEnvironmentProperties(envProps);
        return contextSource;
    }

    //多个文件 放入文件夹压缩成压缩包并下载
    public void zipFiles(List<Map<String, Object>> fileList, HttpServletResponse response, String mkdir) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            //下载压缩包
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("附件.zip", "UTF-8"));
            log.info("开始循环压缩");
            // 创建 ZipEntry 对象
            for (Map map : fileList) {
                String fileName = map.get("fileName").toString();
                if (mkdir.equals("3")) {
                    String projectName = "自定义目录";
                    fileName =projectName+ File.separator +map.get("fileType").toString() + File.separator + map.get("fileName").toString();
                }
                if (mkdir.equals("2")) {
                    fileName = map.get("fileType").toString() + File.separator + map.get("fileName").toString();
                }
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write((byte[]) map.get("stream"));
            }
            log.info("结束循环压缩");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            log.info("压缩异常");
        }
    }

    public ByteArrayOutputStream reader(InputStream input) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = input.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            log.info("文件复制异常");
            throw new Exception("Illegal flow.");
        } finally {
            log.info("文件复制完成");
            org.apache.commons.compress.utils.IOUtils.closeQuietly(input);
        }
        return byteArrayOutputStream;
    }

    /**
     * List数组 生成树形结构
     * @param list
     * @return
     */
    @Override
    public Map<String, Object> getTreeMap(List<Map<String, Object>> list) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Map<String, Object> entity : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("isNode", entity.get("isNode"));
            map.put("id", entity.get("id"));
            map.put("nodeId", entity.get("nodeId"));
            map.put("nodeParentId", entity.get("nodeParentId"));
            map.put("sortNum", entity.get("sortNum"));
            map.put("projectName", entity.get("projectName"));
            resultMap.put("nodeId", map);
            mapList.add(resultMap);
        }
        resultMap = getTreeMap(mapList, resultMap);
        return resultMap;
    }

    @Override
    public void updateDDL(String sql) {
        jdbcTemplate.execute(sql);
    }

    public Map<String, Object> getTreeMap(List<Map<String, Object>> mapList,Map<String, Object> resultMap){
        List<String> idList = new ArrayList<>();
        for (Map map : mapList) {
            //获取父id
            String parentId = map.get("nodeParentId").toString();
            if (resultMap.get(parentId) != null) {
                //获取父节点
                Map<String, Object> parentMap = (Map<String, Object>) resultMap.get(parentId);
                //获取父节点的子节点
                List<Map<String, Object>> childrenList;
                if (parentMap.get("children") != null) {
                    childrenList = (List<Map<String, Object>>) parentMap.get("children");
                } else {
                    childrenList = new ArrayList<>();
                }
                //将当前的对象添加到父对象的children中
                childrenList.add(map);
                parentMap.put("children", childrenList);
                idList.add(map.get("id") != null ? map.get("id").toString() : "");
            }
        }
        for (String id : idList) {
            resultMap.remove(id);
        }
        return resultMap;
    }

    /**
     * 连接外部数据库
     *
     * @return
     */
    public DataSource secondDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(passWord);
        return dataSource;
    }

    /**
     * 连接第二个数据源
     *
     * @return
     */
    public JdbcTemplate getLvJdbcTemplate() {
        return new JdbcTemplate(secondDataSource());
    }

    @Override
    public List<Map<String, Object>> getUpdateExcel(MultipartFile file){
        return updateExcel(file);
    }

    @Override
    public List<String> generalImport(String tableCode, MultipartFile file, String userId, String schemeNo, String remarkNo, String type, String informationType) {
        List<String> msgList = new ArrayList<>();
        // 通过execl返回对象数组
        List<Map<String, Object>> dataList = updateExcel(file);
        // 临时存储保存对象数组
        List<Map<String, Object>> saveList = new ArrayList<>();
        String schemaCode = tableCode;
        int row = 2;
        for (Map<String, Object> map : dataList) {
            String err = "第" + row + "行导入校验错误,错误信息为：";
            // 表-批量导入模板 示例
            if ("test".equals(tableCode)) {
                err = checkHumanGeneticMaterial(err, map, saveList, schemeNo, remarkNo);
            }
            // string verification
            boolean changeFlag = Pattern.matches(ANCHOR_POINT, err);
            if (!changeFlag) {
                msgList.add(err);
            }
            row++;
        }
        if (msgList.isEmpty()) {
            for (Map<String, Object> saveMap : saveList) {
                // 保存对象
                saveObj(saveMap, schemaCode, userId);
            }
        }
        return msgList;
    }

    /**
     * 导入校验列子
     * @param err
     * @param map
     * @param saveList
     * @param schemeNo
     * @param remarkNo
     * @return
     */
    private String checkHumanGeneticMaterial(String err, Map<String, Object> map, List<Map<String, Object>> saveList, String schemeNo, String remarkNo) {
        Map<String, Object> saveMap = new HashMap<>();
        //required field check
        err = checkField(err, map, 'A', 'A');
        err = checkField(err, map, 'C', 'I');
        //column required field
        Object letterA = map.get("A");
        Object letterB = map.get("B");
        Object letterC = map.get("C");
        Object letterD = map.get("D");
        Object letterF = map.get("F");
        Object letterG = map.get("G");
        Object letterI = map.get("I");
        //check enum
        String[] listA = {"全血", "血清", "血浆", "尿液", "粪便", "血细胞", "脑脊液", "骨髓", "骨髓涂片", "血涂片", "组织切片", "其他样本"};
        err = checkEnum(listA, letterA, err, "A");
        String[] listG = {"重要遗传家系", "特定地区人类遗传资源", "科技部规定种类、数量的人类遗传资源"};
        err = checkEnum(listG, letterG, err, "G");
        String[] listI = {"检测后立即销毁", "在XX单位下保存至XX之后销毁", "返样至XX单位", "其他"};
        err = checkEnum(listI, letterI, err, "I");
        // other check
        boolean regexAB = letterA != null && Pattern.matches("其他样本", letterA.toString());
        if (regexAB && letterB == null) {
            err += "“人类遗传资源名称”列选择“其他样本”时，B列必填";
        }
        if (regexAB && letterB != null) {
            saveMap.put("other", letterB);
        }
        Map<String, Object> checkIntegerMap = new HashMap<>();
        checkIntegerMap.put("C", letterC);
        checkIntegerMap.put("D", letterD);
        err = batchCheckRegular(err, checkIntegerMap, POSITIVE_INT, "列请填入正整数");
        checkIntegerMap.clear();
        checkIntegerMap.put("", letterF);
        err = batchCheckRegular(err, checkIntegerMap, BIG_THAN_ZERO, "“单位规格”列请填入大于等于0的数字");
        boolean regexA = letterA != null && letterF != null && Pattern.matches("骨髓涂片|血涂片|组织切片", letterA.toString()) && !Pattern.matches(".*/*.*/*.*", letterF.toString());
        if (regexA) {
            err = "当“人类遗传资源名称”列选择“骨髓涂片/血涂片/组织切片”时，请按照XX*XX*XX格式输入;";
        }
        Object letterH = map.get("H");
        boolean bRequiredField = letterA != null && letterA.toString().equals("其他样本");
        if (bRequiredField) {
            saveMap.put("other", letterB);
        }
        if (bRequiredField && letterB == null) {
            err += " “人类遗传资源名称”列选择“其他样本”时，B列必填;";
        }
        if (letterI == null) {
            return err;
        }
        Object letterJ = map.get("J");
        Object letterK = map.get("K");
        Map<String, Object> companyMap = new HashMap<>();
        List<String> companyList = new ArrayList<>();
        boolean regexI = Pattern.matches("在XX单位下保存至XX之后销毁", letterI.toString());
        if (regexI && letterJ != null && letterK != null) {
            boolean regexCompanyL = !companyList.contains(letterJ);
            if (regexCompanyL) {
                err += "J列请填写“一、基本信息表/参与采集单位信息”模块的单位全称;";
            } else {
                String value = "在" + companyMap.get(letterJ) + "单位下保存至" + letterK + "之后销毁";
                saveMap.put("disposalPlan", value);
            }
        }
        if (regexI && (letterJ == null || letterK == null)) {
            err += " “处置方案”列选择“在XX单位下保存至XX之后销毁”时，J、K列必填;";
        }
        Object letterL = map.get("L");
        boolean regexL = Pattern.matches("返样至XX单位", letterI.toString());
        if (regexL && letterL != null) {
            boolean regexCompanyL = !companyList.contains(letterL);
            if (regexCompanyL) {
                err = "L列请填写“一、基本信息表/参与采集单位信息”模块的单位全称;";
            } else {
                saveMap.put("disposalPlan", companyMap.get(letterL));
            }
        }
        if (regexL && letterL == null) {
            err += " “处置方案”列选择“返样至XX单位”时，L列必填;";
        }
        Object letterM = map.get("M");
        boolean mRequiredField = letterI.toString().equals("其他");
        if (mRequiredField) {
            saveMap.put("disposalPlan", letterM);
        }
        if (mRequiredField && letterM == null) {
            err += " “处置方案”列选择“其他”时，M列必填;";
        }
        boolean changeFlag = Pattern.matches(ANCHOR_POINT, err);
        if (changeFlag) {
            saveMap.put(SCHEME_NO, schemeNo);
            saveMap.put(REMARK_NO, remarkNo);
            saveMap.put("nameOfHumanGeneticResour", letterA);
            saveMap.put("numberOfSingleCases", letterC);
            saveMap.put("numberOfCases", letterD);
            saveMap.put("unitSpecification", letterF);
            saveMap.put("sampleType", letterG);
            saveMap.put("acquisitionUnit", letterH);
            saveMap.put("disposalPlan", letterI);
            saveMap.put("isActive", "0");
            saveList.add(saveMap);
        }
        return err;
    }

    public void saveObj(Map<String, Object> map, String targetSchemaCode, String id) {
        map.remove("modifier");
        map.remove("ownerDeptId");
        map.remove("owner");
        map.remove("createDeptId");
        map.remove("creater");
        boolean flag = false;
        // 自定义保存方法
    }
}
