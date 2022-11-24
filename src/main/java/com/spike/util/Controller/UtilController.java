package com.spike.util.Controller;

import com.alibaba.fastjson.JSON;
import com.spike.util.Service.UtilService;
import com.spike.util.Util.QRCodeUtil;
import com.spike.util.UtilClass.ResponseResult;
import com.spike.util.entry.Person;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/UtilController")
public class UtilController {

    public static final String SUCCESS = "操作成功!";

    @Resource
    private UtilService utilService;

    /**
     * 通用导出excel接口
     *
     * @param resMap
     * @param response
     * @return
     * @throws Exception 请求示例
     *                   {
     *                   //字段对应编码
     *                   "tableCode": [
     *                   "dealer_code",
     *                   "dealer_name",
     *                   "dealer_abbreviation",
     *                   "region",
     *                   "sub_region",
     *                   "feedback_status",
     *                   "feedback_date",
     *                   "total_units",
     *                   "cayenne_numbers",
     *                   "nine_one_eight_numbers",
     *                   "sevent_one_eight_number",
     *                   "boxSter_cayman_number",
     *                   "taycan_numbers",
     *                   "panamera_numbers",
     *                   "total_est_MSRP"
     *                   ],
     *                   //excel表头名称
     *                   "tableName": [
     *                   "经销商编号",
     *                   "经销商名称",
     *                   "经销商简称",
     *                   "区域",
     *                   "子区域",
     *                   "反馈状态（首次）",
     *                   "反馈时间（首次）",
     *                   "总台数",
     *                   "Macan 台数",
     *                   "Cayenne 台数",
     *                   "918 台数",
     *                   "718 台数",
     *                   "BoxSter/Cayman 台数",
     *                   "Taycan 台数",
     *                   "Panamera 台数",
     *                   "总计Est. MSRP"
     *                   ],
     *                   //excel数据
     *                   "data": [
     *                   {
     *                   "dealer_code": "7000210",
     *                   "dealer_name": "济南济西保时捷中心",
     *                   "dealer_abbreviation": "JNJX",
     *                   "region": "North",
     *                   "sub_region": "N3",
     *                   "feedback_status": "已拒绝",
     *                   "feedback_date": "2022-08-10 14:30:30",
     *                   "total_units": 4,
     *                   "macan_numbers": 2,
     *                   "cayenne_numbers": 1,
     *                   "nine_one_eight_numbers": null,
     *                   "sevent_one_eight_number": 1,
     *                   "boxSter_cayman_number": null,
     *                   "taycan_numbers": null,
     *                   "panamera_numbers": null,
     *                   "total_est_MSRP": 2596897.64
     *                   }
     *                   ]
     *                   }
     */
    @ApiOperation("exportExcel")
    @PostMapping("/exportExcel")
    public ResponseResult<Object> exportExcel(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.exportExcel("excelTemplate", resMap, response);
        return this.getResponseResult(SUCCESS);
    }

    /**
     * 通用发送邮件接口
     *
     * @param map
     * @return 请求示例:
     * {
     * "emailTitle":"测试标题",
     * "emailText":"测试邮件内容",
     * "addressee":["XXXXX@qq.com"]
     * }
     */
    @ApiOperation("sendEmail")
    @PostMapping("/sendEmail")
    public ResponseResult<Object> sendEmail(@RequestBody Map<String, Object> map) {
        String emailTitle = map.get("emailTitle").toString();
        String emailText = map.get("emailText").toString();
        List<String> addressee = (List<String>) map.get("addressee");
        utilService.sendEmail(emailTitle, emailText, addressee);
        //Operation success feedback
        return this.getResponseResult(SUCCESS);
    }

    /**
     * 将多个文件打包成zip压缩包
     *
     * @param resMap
     * @param response
     * @return
     * @throws Exception 请求示例:
     *                   {
     *                   "attachList": [
     *                   {
     *                   "fileName":"测试文件.docx",
     *                   "fileId": "DIRE_db5d502962064ddb844d47063a1f6b4a子表数据回写报表二开.docx",
     *                   "stream":比特数组 ByteArrayOutputStream类型
     *                   }
     *                   ]
     *                   }
     */
    @ApiOperation("generateAttachmentPackage")
    @PostMapping("/generateAttachmentPackage")
    public ResponseResult generateAttachmentPackage(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.generateAttachmentPackage(resMap, response);
        return this.getResponseResult(SUCCESS);
    }

    protected ResponseResult<Object> getResponseResult(String errMsg) {
        return ResponseResult.builder().errcode(0l).errmsg(errMsg).build();
    }

    protected ResponseResult<Object> getResponseResult(Object object, String errMsg) {
        return ResponseResult.builder().data(object).errcode(0l).errmsg(errMsg).build();
    }

    protected ResponseResult<Object> getErrResponseResult(Object object, long code, String errMsg) {
        return ResponseResult.builder().data(object).errcode(code).errmsg(errMsg).build();
    }

    @ApiOperation("allFileDownload")
    @GetMapping("/allFileDownload")
    public void allFileDownload(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "filePath") String filePath) throws Exception {
        String fileDir = getFileDir(filePath);
        log.error("************************:  " + fileDir + fileName + " *******************************");
        File newFile = new File(fileDir, fileName);
        log.error("************************:  开始查找文件 *******************************");
    }

    public String getFileDir(String filePath) {
        List<String> url = Arrays.asList(filePath.split("/"));
        String path = "";
        for (String str : url) {
            str = str + File.separator;
            path = path + str;
        }
        return path;
    }

    @GetMapping("/ldapFindAll")
    public List<Person> ldapFindAll() {
        return utilService.findAll();
    }

    @GetMapping("/updateDDL")
    public ResponseResult<Object> updateDDL(@RequestParam(value = "sql") String sql) {
        utilService.updateDDL(sql);
        return getResponseResult(SUCCESS);
    }


    @ApiOperation("getUpdateExcel")
    @PostMapping("/getUpdateExcel")
    public ResponseResult<Object> getUpdateExcel(@RequestParam MultipartFile file) {
        List<Map<String, Object>> listMap = utilService.getUpdateExcel(file);
        return this.getResponseResult(listMap, SUCCESS);
    }

    /**
     * 请求示例
     * <p>
     * result风格 Post
     * 数据传输格式  form-data
     * <p>
     * 入参示例：
     * <p>
     * file:
     * schemaCode:schedule_human_geneti_info
     * schemeNo:12bd59f2cfe74f319e0a179cf568a157
     * remarkNo:qwer1234       //
     * type: xxx //分类字段 必填 可为空
     * informationType：xxx //分类字段  可为空
     *
     * @param schemaCode
     * @param file
     * @param schemeNo
     * @param remarkNo
     * @param type
     * @param informationType
     * @param request
     * @return
     */
    @ApiOperation(value = "通用导入接口", httpMethod = "POST")
    @PostMapping("/generalImport")
    public ResponseResult<Object> generalImport(@RequestParam("schemaCode") String schemaCode, @RequestParam("file") MultipartFile file, @RequestParam("schemeNo") String schemeNo, @RequestParam("remarkNo") String remarkNo, @RequestParam("type") String type, @RequestParam("informationType") String informationType, HttpServletRequest request) {
        if (schemaCode == null) {
            return this.getErrResponseResult("SchemaCode Is Null!", -1l, SUCCESS);
        }
        if (file == null) {
            return this.getErrResponseResult("File Is Null!", -1l, SUCCESS);
        }
        if (schemeNo == null) {
            return this.getErrResponseResult("SchemeNo Is Null!", -1l, SUCCESS);
        }
        if (remarkNo == null) {
            return this.getErrResponseResult("RemarkNo Is Null!", -1l, SUCCESS);
        }
        List<String> msgList = utilService.generalImport(schemaCode, file, "userId", schemeNo, remarkNo, type, informationType);
        if (!msgList.isEmpty()) {
            return this.getErrResponseResult(msgList, -1l, SUCCESS);
        }
        return this.getResponseResult(msgList, SUCCESS);
    }

    /**
     *
     * @param map
     * @return
     */
    @PostMapping("/getQRCode")
    public ResponseResult<Object> getWebQR(@RequestBody Map<String,Object> map) {
        String QRCodeMsgName = map.get("QRCodeMsgName").toString();
        String QRCodeName = map.get("QRCodeName").toString();
        String url = map.get("URL").toString();
        String dir = map.get("DIR").toString();
        log.info("map：{}", JSON.toJSONString(map));
        BufferedImage image = QRCodeUtil.createImage("utf-8", url, 300, 300);
        QRCodeUtil.addUpFont(image, QRCodeMsgName);
        Date nowDate = new Date();
        String path = dir+File.separator+QRCodeName;
        if(map.get("isRandom")!=null){
            path+=nowDate.getTime();
        }
        path+=".jpg";
        log.info("文件路径：{}",path);
        try {
            File file = new File(path);
            if (!file.isDirectory()) {
                file.mkdirs();
            }
            ImageIO.write(image, "JPEG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.clear();
        map.put("msg", "获取二维码成功");
        map.put("imagePath", path);
        return this.getResponseResult(map, SUCCESS);
    }

}
