package com.spike.util.Service.Impl;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ExcelUtil;
import com.spike.util.entry.Person;
import com.spike.util.ldapConfig.SsldapContextSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;




@Service
@Slf4j
public class UtilServiceImpl implements UtilService {
    @Resource
    private JdbcTemplate jdbcTemplate;

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
                if(!value.isEmpty()&&StringUtils.isNumeric(value)){
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
}
