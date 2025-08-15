package org.example.userservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.userservice.util.NullValue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);
    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final long CACHE_NULL_TTL = 2; // 防穿透空值缓存的过期时间（分钟）

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(cacheLock)")
    public Object around(ProceedingJoinPoint joinPoint, CacheLock cacheLock) throws Throwable {
        // 1. 获取方法签名和注解参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 假设缓存的 key 总是基于第一个参数 (例如 ID)
        if (args.length == 0) {
            // 如果方法没有参数，则无法生成 key，直接执行原方法
            return joinPoint.proceed();
        }
        String id = args[0].toString();
        String cacheKey = cacheLock.prefix() + id;

        // 2. 查询缓存
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            log.info("AOP - Cache hit for key: {}", cacheKey);
            return (cachedValue instanceof NullValue) ? null : cachedValue;
        }

        // 3. 缓存未命中，获取分布式锁
        String lockKey = LOCK_KEY_PREFIX + cacheLock.prefix() + id;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (isLocked) {
                log.info("AOP - Lock acquired for key: {}", cacheKey);
                // 4. 获取锁成功，执行原方法查询数据库
                Object dbValue = joinPoint.proceed();

                // 5. 将结果存入缓存
                if (dbValue != null) {
                    redisTemplate.opsForValue().set(cacheKey, dbValue, cacheLock.ttl(), cacheLock.unit());
                    log.info("AOP - Cache rebuilt for key: {}", cacheKey);
                } else {
                    // 防缓存穿透，缓存空值
                    redisTemplate.opsForValue().set(cacheKey, NullValue.INSTANCE, CACHE_NULL_TTL, TimeUnit.MINUTES);
                    log.warn("AOP - DB result is null, caching NullValue for key: {}", cacheKey);
                }
                return dbValue;
            } else {
                // 6. 未获取到锁，休眠后重试（自旋）
                log.warn("AOP - Failed to acquire lock for key: {}, retrying after sleep.", cacheKey);
                Thread.sleep(50);
                // 注意：这里递归调用了被AOP代理的方法，而不是原始方法
                return around(joinPoint, cacheLock);
            }
        } finally {
            // 7. 释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("AOP - Lock released for key: {}", cacheKey);
            }
        }
    }
}
