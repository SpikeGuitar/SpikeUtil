package com.spike.util.Controller;

import com.spike.util.Service.UtilService;
import com.spike.util.UtilClass.ResponseResult;
import com.spike.util.entry.Person;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/UtilController")
public class UtilController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public static final String SUCCESS = "操作成功!";

    @Resource
    private UtilService utilService;

    /**
     * 通用导出excel接口
     *
     * @param resMap
     * @param response
     * @return
     * @throws Exception
     *
     * 请求示例
     * {
     * //字段对应编码
     *     "tableCode": [
     *         "dealer_code",
     *         "dealer_name",
     *         "dealer_abbreviation",
     *         "region",
     *         "sub_region",
     *         "feedback_status",
     *         "feedback_date",
     *         "total_units",
     *         "cayenne_numbers",
     *         "nine_one_eight_numbers",
     *         "sevent_one_eight_number",
     *         "boxSter_cayman_number",
     *         "taycan_numbers",
     *         "panamera_numbers",
     *         "total_est_MSRP"
     *     ],
     *     //excel表头名称
     *     "tableName": [
     *         "经销商编号",
     *         "经销商名称",
     *         "经销商简称",
     *         "区域",
     *         "子区域",
     *         "反馈状态（首次）",
     *         "反馈时间（首次）",
     *         "总台数",
     *         "Macan 台数",
     *         "Cayenne 台数",
     *         "918 台数",
     *         "718 台数",
     *         "BoxSter/Cayman 台数",
     *         "Taycan 台数",
     *         "Panamera 台数",
     *         "总计Est. MSRP"
     *     ],
     *     //excel数据
     *     "data": [
     *         {
     *             "dealer_code": "7000210",
     *             "dealer_name": "济南济西保时捷中心",
     *             "dealer_abbreviation": "JNJX",
     *             "region": "North",
     *             "sub_region": "N3",
     *             "feedback_status": "已拒绝",
     *             "feedback_date": "2022-08-10 14:30:30",
     *             "total_units": 4,
     *             "macan_numbers": 2,
     *             "cayenne_numbers": 1,
     *             "nine_one_eight_numbers": null,
     *             "sevent_one_eight_number": 1,
     *             "boxSter_cayman_number": null,
     *             "taycan_numbers": null,
     *             "panamera_numbers": null,
     *             "total_est_MSRP": 2596897.64
     *         }
     *     ]
     * }
     */
    @ApiOperation("exportExcel")
    @PostMapping("/exportExcel")
    public ResponseResult<Object> exportExcel(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.exportExcel("excelTemplate",resMap,response);
        return this.getErrResponseResult(SUCCESS);
    }

    /**
     * 通用发送邮件接口
     *
     * @param map
     * @return
     *
     * 请求示例:
     * {
     *     "emailTitle":"测试标题",
     *     "emailText":"测试邮件内容",
     *     "addressee":["XXXXX@qq.com"]
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
        return this.getErrResponseResult(SUCCESS);
    }

    /**
     * 将多个文件打包成zip压缩包
     *
     * @param resMap
     * @param response
     * @return
     * @throws Exception
     *
     *
     * 请求示例:
     * {
     *     "attachList": [
     *         {
     *             "fileName":"测试文件.docx",
     *             "fileId": "DIRE_db5d502962064ddb844d47063a1f6b4a子表数据回写报表二开.docx",
     *             "stream":比特数组 ByteArrayOutputStream类型
     *         }
     *     ]
     * }
     */
    @ApiOperation("generateAttachmentPackage")
    @PostMapping("/generateAttachmentPackage")
    public ResponseResult generateAttachmentPackage(@RequestBody Map<String, Object> resMap, HttpServletResponse response) throws Exception {
        utilService.generateAttachmentPackage(resMap,response);
        return this.getErrResponseResult(SUCCESS);
    }

    protected ResponseResult<Object> getErrResponseResult(String errMsg) {
        return ResponseResult.builder().errcode(0l).errmsg(errMsg).build();
    }

    @ApiOperation("allFileDownload")
    @GetMapping("/allFileDownload")
    public void allFileDownload(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "filePath") String filePath) throws Exception {
        String fileDir = getFileDir(filePath);
        log.error("************************:  "+fileDir+fileName+" *******************************");
        File newFile = new File(fileDir, fileName);
        log.error("************************:  开始查找文件 *******************************");
    }

    public String getFileDir(String filePath) {
        List<String> url = Arrays.asList(filePath.split("/"));
        String path = "";
        for(String str :url){
            str = str+File.separator;
            path = path+str;
        }
        return path;
    }

    @GetMapping("/ldapFindAll")
    public List<Person> ldapFindAll() {
        return utilService.findAll();
    }

    @GetMapping("/updateDDL")
    public ResponseResult<Object> updateDDL(@RequestParam(value = "sql") String sql) {
        jdbcTemplate.execute(sql);
        return getErrResponseResult(SUCCESS);
    }

}
