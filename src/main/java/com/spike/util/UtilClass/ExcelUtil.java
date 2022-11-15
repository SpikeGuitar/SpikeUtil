package com.spike.util.UtilClass;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
         * @param response     响应体
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

    /**
     * 获取工作簿workbook
     *
     * @param file
     * @return
     */
    public static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        Sheet sheet = null;
        try (InputStream is = file.getInputStream()) {
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith("xls")) {
                //2003
                POIFSFileSystem poifsFileSystem = new POIFSFileSystem(is);
                workbook = new HSSFWorkbook(poifsFileSystem);
                sheet = workbook.getSheetAt(0);
            } else if (fileName.endsWith("xlsx")) {
                //2007 及2007以上
                workbook = new XSSFWorkbook(is);
                sheet = workbook.getSheetAt(0);
            }
        } catch (IOException e) {
            log.info("", e);
            e.printStackTrace();
        }
        return workbook;
    }

    /**
     * 获取单元格的值
     *
     * @param cell
     * @return
     */
    public static String getValue(Cell cell) {
        if (cell.getCellTypeEnum() == org.apache.poi.ss.usermodel.CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellTypeEnum() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            String value = "";
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                Date d = cell.getDateCellValue();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                value = sdf.format(d);
            } else {
                double temp = cell.getNumericCellValue();
                //value = new BigDecimal(temp).toString();
                value = String.valueOf(temp);
            }
            return value;
        } else if (cell.getCellTypeEnum() == org.apache.poi.ss.usermodel.CellType.STRING) {
            return String.valueOf(cell.getStringCellValue());
        } else {
            return String.valueOf(cell.getStringCellValue());
        }
    }

    /**
     * 将 execl 文件转成List<Map<String, Object>>
     *
     * @param file
     * @return
     */
    public static List<Map<String, Object>> updateExcel(MultipartFile file) {
        Workbook workbook = getWorkBook(file);//获取工作簿workbook
        Sheet sheetAt = workbook.getSheetAt(0);//根据工作簿获取整张excel表的信息
        List<Map<String, Object>> resultMap = new ArrayList<>();
        for (int i = 1; i <= sheetAt.getLastRowNum(); i++) {//第一行是表头，所以不要，i从1开始
            Map<String, Object> tempMap = new HashMap<>();
            for (int j = 0; j < sheetAt.getRow(i).getLastCellNum(); j++) {//循环每一行
                Cell cell = sheetAt.getRow(i).getCell(j);//获取每一个单元格的值
                String value = getValue(cell);//把单元格的值转成字符串
                tempMap.put(String.valueOf(j), value);
            }
            resultMap.add(tempMap);
        }
        return resultMap;
    }

    //      boolean regexIJ = Pattern.matches("在XX单位下保存至XX之后销毁", letterH.toString()); 正则表达式匹配

    /**
     * Batch check regular expression
     *
     *         Map<String,Object> checkIntegerMap = new HashMap<>();
     *         checkIntegerMap.put("A",letterA);
     *         checkIntegerMap.put("C",letterC);
     *         checkIntegerMap.put("D",letterD);
     *         err = batchCheckPositiveInteger(err,checkIntegerMap,POSITIVE_INT,"列请填入正整数");
     *
     * @param err
     * @param map
     * @param regular
     * @param msg
     * @return
     */
    public static String batchCheckRegular(String err,Map<String,Object> map,String regular,String msg){
        for( Map.Entry<String,Object> entry :map.entrySet()){
            Object value = entry.getValue();
            String key = entry.getKey();
            boolean checkFlag = value != null && !Pattern.matches(regular, value.toString());
            if (checkFlag) {
                err += key+msg+";";
            }
        }
        return err;
    }

    /**
     *Null value verification
     *
     *err = checkField(err, map, 'A', 'F');
     *
     * @param err
     * @param map
     * @param start
     * @param end
     * @return
     */
    public static String checkField(String err, Map<String, Object> map, char start, char end) {
        for (char c = start; c <= end; c++) {
            String letter = String.valueOf(c);
            if (map.get(letter) == null || map.get(letter) != null && map.get(letter).toString().isEmpty()) {
                err += letter + "列为必填项;";
            }
        }
        return err;
    }

    /**
     * Check the specified enumeration
     *
     * String[] listE = {"诊断性生物标志物", "监测性生物标志物", "药效性/反应生物标志物", "预测性生物标志物", "预后生物标志物", "安全性生物标志物","易感性/风险生物标志物"};
     * err = checkEnum(listE, map.get("E"), err, "E");
     *
     * @param list
     * @param letter
     * @param err
     * @param colStr
     * @return
     */
    public static String checkEnum(String[] list, Object letter, String err, String colStr) {
        List<String> arr = Arrays.asList(list);
        if (!arr.contains(letter)) {
            err += colStr + "列请在下拉框中指定的选项中选择;";
        }
        return err;
    }

}
