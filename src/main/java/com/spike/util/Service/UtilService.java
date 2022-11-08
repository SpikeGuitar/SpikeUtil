package com.spike.util.Service;

import com.spike.util.entry.Person;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface UtilService {
    void exportExcel(String execlTempleName, Map<String, Object> map, HttpServletResponse response) throws Exception;

    String sendEmail(String emailTitle, String emailText, List<String> addressee);

    void generateAttachmentPackage(Map<String, Object> map, HttpServletResponse response) throws Exception;

    List<Person> findAll();

    public Map<String, Object> getTreeMap(List<Map<String, Object>> list);
}
