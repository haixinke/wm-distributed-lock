package com.wm.spring.boot.autoconfigure.lock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author wangmin
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface WmLock {

    /**
     * 前缀
     * @return
     */
    String prefix() default "";

    /**
     * 关键字key
     * @return
     */
    String key();

    /**
     * 超时时间，单位毫秒
     * @return
     */
    long expire() default 10000;

    /**
     * 单次睡眠时间，单位毫秒
     * @return
     */
    long sleepMills() default 500;

    /**
     * 重试次数
     * @return
     */
    int retryTimes() default 20;
}
