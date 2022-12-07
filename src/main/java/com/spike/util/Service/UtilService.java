package com.spike.util.Service;

import com.spike.util.UtilClass.ResponseResult;
import com.spike.util.entry.Person;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


public interface UtilService {
    void exportExcel(String execlTempleName, Map<String, Object> map, HttpServletResponse response) throws Exception;

    String sendEmail(String emailTitle, String emailText, List<String> addressee);

    void generateAttachmentPackage(Map<String, Object> map, HttpServletResponse response) throws Exception;

    List<Person> findAll();

    Map<String, Object> getTreeMap(List<Map<String, Object>> list);

    void updateDDL(String sql);

    List<Map<String, Object>> getUpdateExcel(MultipartFile file);

    void fileDownload(String path, HttpServletResponse response) throws Exception;

    List<String> generalImport(String schemaCode, MultipartFile file, String userId, String schemeNo, String remarkNo, String type, String informationType);

    Map<String, Object> getWebQR(Map<String, Object> map);

    String getDownloadUrl(String toString) throws UnknownHostException;

    String upFile(String url, MultipartFile file) throws IOException;

    ResponseResult<Object> userIsRead(List<Map<String, Object>> mapList, String userId);

    ResponseResult<Object> userIsReadState(List<Map<String, Object>> mapList,String userId);
}
