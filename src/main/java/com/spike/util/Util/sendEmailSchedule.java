package com.spike.util.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class sendEmailSchedule{

    @Resource
    private RedisTemplate<String, Boolean> redisTemplate;

    @Value("${sync.departId:}")
    private String departId;

    /**
     * 每天2点同步用户信息
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void synchronizeUsers() {
        LockUtil lockUtil = new LockUtil("sendEmail", redisTemplate);
        boolean flag = false;
        try {
            flag = lockUtil.lock();
            if (flag) {
                System.out.println(departId);
            }
        }catch (Exception e){
            Date curTime = new Date();
            log.info("sendEmailSchedule sendEmail err time: {}", curTime);
        }finally {
            if (false) {
                lockUtil.unLock();
            }
        }
    }

}
