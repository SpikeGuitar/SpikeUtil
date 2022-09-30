package com.spike.util.UtilClass;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * @author zhengqiu
 * @date 2022/4/29 14:43
 */
@Slf4j
public class ExcelUtil {

    /**
     * 判断excel文件类型正则表达式
     */
    private static final String IS_EXCEL = "^.+\\.(?i)((xls)|(xlsx))$";

    /**
     * 判断是否为 xls后缀版本的excel
     */
    private static final String IS_XLS_EXCEL = "^.+\\.(?i)(xls)$";

    /**
     * 存放excel模板的目录
     */
    private static final String CLASS_PATH = "templates/excel/";

    /**
     * 构建 Workbook 对象
     *
     * @param templateName 模板文件名
     * @return Workbook
     */
    public static Workbook buildWorkbook(String templateName) throws Exception {
        Workbook workbook;
        try {
            if (templateName.matches(IS_EXCEL)) {
                // 得到文件输入流对象
                InputStream inputStream = ExcelUtil.class.getClassLoader().getResourceAsStream(CLASS_PATH + templateName);
                if (inputStream == null) {
                    throw new Exception("找不到excel模板文件");
                }

                boolean isXlsExcel = templateName.matches(IS_XLS_EXCEL);
                // 创建工作簿，并传递要读取的文件
                workbook = isXlsExcel ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
                inputStream.close();
            } else {
                throw new Exception("文件格式不是xls或xlsx");
            }
            return workbook;
        } catch (Exception e) {
            log.info("获取Workbook对象异常！msg：{}", e.getMessage(), e);
            throw e;
        }

    }

    /**
     * 获取response输出流
     *
     * @param fileName 文件名
     * @param response 响应体
     * @return OutputStream
     */
    private static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        try {
            fileName = URLEncoder.encode(fileName + ".xlsx", "UTF-8");
            response.setContentType("octets/stream");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ";" + "filename*=utf-8''" + fileName);
            return response.getOutputStream();
        } catch (IOException e) {
            log.error("设置excel输出流异常！文件名：{}", fileName, e);
            throw e;
        }
    }
    /**
     * excel 模板类
     */
    public static class ExcelTemplate {

        private Workbook workbook;

        /**
         * 通过文件名称读取模板文件并初始化ExcelTemplate对象
         *
         * @param fileName 模板文件名
         * @return ExcelTemplate
         */
        public static ExcelTemplate init(String fileName) throws Exception {
            ExcelTemplate excelTemplate = new ExcelTemplate();
            Workbook workbook = ExcelUtil.buildWorkbook(fileName);
            excelTemplate.setWorkbook(workbook);
            return excelTemplate;
        }

        /**
         * 导出excel
         *
         * @param response 响应体
         * @param newExcelName 自定义导出excel的文件名
         */
        public void export(HttpServletResponse response, String newExcelName) throws Exception {
            OutputStream out = null;
            try {
                out = getOutputStream(newExcelName, response);
                ByteArrayOutputStream ops = new ByteArrayOutputStream();
                workbook.write(ops);
                out.write(ops.toByteArray());
            } finally {

                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void setWorkbook(Workbook workbook) {
            this.workbook = workbook;
        }

        public Workbook getWorkbook() {
            return workbook;
        }
    }

}
