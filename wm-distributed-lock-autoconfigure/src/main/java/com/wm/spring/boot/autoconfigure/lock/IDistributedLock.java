package com.wm.spring.boot.autoconfigure.lock;

/**
 * Desc: 分布式锁接口
 * User: wangmin
 * Date: 2020/3/27
 * Time: 10:33 上午
 */
public interface IDistributedLock {

    /**
     * 获得锁操作
     * @param key
     * @param expire 过期时间（毫秒）
     * @param retryTimes 重试次数
     * @param sleepMillis 睡眠时间（毫秒）
     * @return
     */
    boolean lock(String key, long expire, int retryTimes, long sleepMillis);

    /**
     * 释放锁
     * @param key
     * @return
     */
    boolean releaseLock(String key);
}
