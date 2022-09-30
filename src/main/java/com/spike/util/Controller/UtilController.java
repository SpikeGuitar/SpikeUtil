package com.spike.util.Controller;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ResponseResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/UtilController")
public class UtilController {

    public static final String SUCCESS = "操作成功!";

    @Resource
    private UtilService utilService;

    @ApiOperation("exportExcel")
    @PostMapping("/exportExcel")
    public ResponseResult<Object> exportExcel(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.exportExcel("excelTemplate",resMap,response);
        return this.getErrResponseResult(SUCCESS);
    }

    @ApiOperation("sendEmail")
    @PostMapping("/sendEmail")
    public ResponseResult<Object> sendEmail(@RequestBody Map<String, Object> map) {
        String emailTitle = map.get("emailTitle").toString();
        String emailText = map.get("emailText").toString();
        List<String> addressee = (List<String>) map.get("addressee");
         utilService.sendEmail(emailTitle, emailText, addressee);
        //Operation success feedback
        return this.getErrResponseResult(SUCCESS);
    }

    @ApiOperation("generateAttachmentPackage")
    @PostMapping("/generateAttachmentPackage")
    public ResponseResult generateAttachmentPackage(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.generateAttachmentPackage(resMap,response);
        return this.getErrResponseResult(SUCCESS);
    }

    protected ResponseResult<Object> getErrResponseResult(String errMsg) {
        return ResponseResult.builder().errcode(0l).errmsg(errMsg).build();
    }

}
