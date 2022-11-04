package com.spike.util.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class sendEmailSchedule{

    @Value("${sync.departId:}")
    private String departId;

    /**
     * 每天2点同步用户信息
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void synchronizeUsers() {
        System.out.println(departId);
    }

}
