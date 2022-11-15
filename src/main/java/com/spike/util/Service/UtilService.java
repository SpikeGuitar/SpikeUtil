package com.spike.util.Service;

import com.spike.util.entry.Person;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface UtilService {
    void exportExcel(String execlTempleName, Map<String, Object> map, HttpServletResponse response) throws Exception;

    String sendEmail(String emailTitle, String emailText, List<String> addressee);

    void generateAttachmentPackage(Map<String, Object> map, HttpServletResponse response) throws Exception;

    List<Person> findAll();

    public Map<String, Object> getTreeMap(List<Map<String, Object>> list);

    void updateDDL(String sql);

    List<Map<String, Object>> getUpdateExcel(MultipartFile file);

    List<String> generalImport(String schemaCode, MultipartFile file, String userId, String schemeNo, String remarkNo, String type, String informationType);
}
