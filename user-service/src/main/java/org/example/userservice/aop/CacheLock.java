package org.example.userservice.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheLock {

    /**
     * Redis 缓存的 Key 前缀.
     * 最终的 Key 将是 "prefix + 方法的第一个参数".
     * 例如: prefix = "user:", id = 1 -> "user:1"
     */
    String prefix();

    /**
     * 缓存的过期时间，默认为 30.
     */
    long ttl() default 30;

    /**
     * 缓存过期时间的单位，默认为分钟.
     */
    TimeUnit unit() default TimeUnit.MINUTES;
}
