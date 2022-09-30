package com.spike.util.Service.Impl;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class UtilServiceImpl implements UtilService {

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
}
