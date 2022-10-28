package com.spike.util.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class LockUtil {

    private RedisTemplate<String, Boolean> redisTemplate;

    private String key;

    public LockUtil(String key, RedisTemplate<String, Boolean> redisTemplate) {
        this.key = key;
        this.redisTemplate = redisTemplate;
    }

    public boolean lock() {
        BoundValueOperations<String, Boolean> lock = redisTemplate.boundValueOps(key);
        log.info("对任务加锁 key=====>{}", key);
        if(lock.get()==null||lock.get()!=null&&!lock.get()){
            lock.set(true);
            return true;
        }
        return false;
    }

    /**
     * 解锁
     */
    public void unLock() {
        BoundValueOperations<String, Boolean> lock = redisTemplate.boundValueOps(key);
        log.info("对任务解锁的 key=====>{}", key);
        lock.set(false);
    }
}