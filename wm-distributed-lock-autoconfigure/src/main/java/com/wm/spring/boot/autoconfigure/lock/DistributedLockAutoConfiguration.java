package com.wm.spring.boot.autoconfigure.lock;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Desc: 锁配置
 * User: wangmin
 * Date: 2020/3/26
 * Time: 3:13 下午
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class DistributedLockAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public IDistributedLock redisDistributedLock(RedisTemplate<String, Object> redisTemplate){
        return new RedisDistributedLock(redisTemplate);
    }

}
