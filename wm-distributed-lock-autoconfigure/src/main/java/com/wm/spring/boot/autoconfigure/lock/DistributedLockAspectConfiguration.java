package com.wm.spring.boot.autoconfigure.lock;

import com.wm.spring.boot.autoconfigure.lock.el.SpringExpressionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Desc: 拦截
 * User: wangmin
 * Date: 2020/3/27
 * Time: 12:58 下午
 */
@Slf4j
@Aspect
@Configuration
@ConditionalOnClass(RedisDistributedLock.class)
@AutoConfigureAfter(DistributedLockAutoConfiguration.class)
public class DistributedLockAspectConfiguration {

    @Autowired
    private IDistributedLock redisDistributedLock;

    @Value("${spring.application.name}")
    private String applicationName;

    private SpringExpressionEvaluator<String> springExpressionEvaluator = new SpringExpressionEvaluator();

    @Pointcut("@annotation(com.wm.spring.boot.autoconfigure.lock.WmLock)")
    private void lockPoint() {
    }

    @Around("lockPoint()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        WmLock igenLock = method.getAnnotation(WmLock.class);
        String key = getKey(igenLock.prefix(), getValue(pjp,igenLock.key()));
        if (StringUtils.isEmpty(key)) {
            Object[] args = pjp.getArgs();
            key = Arrays.toString(args);
        }
        boolean lock = redisDistributedLock.lock(key, igenLock.expire(), igenLock.retryTimes(), igenLock.sleepMills());
        if (!lock) {
            log.info("get lock failed : " + key);
            return null;
        }
        log.debug("get lock success : " + key);
        try {
            return pjp.proceed();
        } catch (Exception e) {
            log.error("execute locked method exception {}", e);
        } finally {
            boolean releaseResult = redisDistributedLock.releaseLock(key);
            log.debug("release lock : " + key + (releaseResult ? " success" : " failed"));
        }
        return null;
    }

    private String getKey(String prefix, String key) {
        if (!StringUtils.isEmpty(prefix)) {
            return applicationName + ":" + prefix + ":" + key;
        } else {
            return applicationName + ":" + key;
        }
    }

    private String getValue(JoinPoint joinPoint, String condition) {
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Class clazz = joinPoint.getTarget().getClass();
        if (args == null || args.length == 0) {
            throw new RuntimeException("EXPRESS_ERROR");
        } else {
            EvaluationContext evaluationContext = this.springExpressionEvaluator.createEvaluationContext(joinPoint.getTarget(), clazz, method, args);
            AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
            return springExpressionEvaluator.condition(condition, methodKey, evaluationContext, String.class);
        }
    }
}
