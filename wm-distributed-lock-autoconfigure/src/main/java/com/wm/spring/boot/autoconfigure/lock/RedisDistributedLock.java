package com.wm.spring.boot.autoconfigure.lock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Desc: redis分布式锁实现
 * User: wangmin
 * Date: 2020/3/26
 * Time: 3:39 下午
 */
@Slf4j
@AllArgsConstructor
public class RedisDistributedLock implements IDistributedLock {

    private RedisTemplate<String, Object> redisTemplate;

    private final static ThreadLocal<String> LOCK_FLAG = new ThreadLocal<>();

    /**
     * 保证谁获得锁，谁释放锁，原子操作
     */
    private final static String UNLOCK_LUA = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        boolean result = setRedis(key, expire);
        while ((!result) && retryTimes-- > 0) {
            try {
                log.info("lock failed, retrying:" + retryTimes);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                return false;
            }
            result = setRedis(key, expire);
        }
        return result;
    }

    private boolean setRedis(String key, long expire) {
        try {
            String result = redisTemplate.execute((RedisCallback<String>) connection -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                String uuid = UUID.randomUUID().toString();
                LOCK_FLAG.set(uuid);
                return commands.set(key, uuid, "NX", "PX", expire);
            });
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            log.error("set redis exception {}", e);
        }
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(LOCK_FLAG.get());
            Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, args);
                } else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
                }
                return 0L;
            });
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("release lock exception {}", e);
        }
        return false;
    }
}
