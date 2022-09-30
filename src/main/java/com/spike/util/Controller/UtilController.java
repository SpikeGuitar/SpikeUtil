package com.spike.util.Controller;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@RestController
@RequestMapping("/UtilController")
public class UtilController {

    public static final String SUCCESS = "操作成功!";

    @Resource
    private UtilService utilService;

    @PostMapping("/exportExcel")
    public ResponseResult<Object> exportExcel(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.exportExcel("excelTemplate",resMap,response);
        return this.getErrResponseResult(0l,SUCCESS);
    }

    protected ResponseResult<Object> getErrResponseResult(Long errCode, String errMsg) {
        return ResponseResult.builder().errcode(errCode).errmsg(errMsg).build();
    }

}
