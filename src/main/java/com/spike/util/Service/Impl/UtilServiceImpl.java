package com.spike.util.Service.Impl;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;




@Service
@Slf4j
public class UtilServiceImpl implements UtilService {

    @Value("${spring.mail.username}")
    private String FROM;

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
                row.createCell(col++).setCellValue(value);
            }
            rowValue = rowValue + 1;
        }
        String fileName = "DFlow" + System.currentTimeMillis();
        excelTemplate.export(response, fileName);
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
}
