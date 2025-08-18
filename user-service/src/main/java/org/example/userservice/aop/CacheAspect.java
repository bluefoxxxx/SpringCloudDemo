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
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        if (args.length == 0) {
            return joinPoint.proceed();
        }

        String id = args[0].toString();
        String cacheKey = cacheLock.prefix() + id;
        String lockKey = "lock:" + cacheKey;

        // 第一次检查缓存
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            log.info("Cache hit for key: {}", cacheKey);
            return (cachedValue instanceof NullValue) ? null : cachedValue;
        }

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (isLocked) {
                log.info("Lock acquired for key: {}", cacheKey);

                // 第二次检查缓存
                cachedValue = redisTemplate.opsForValue().get(cacheKey);
                if (cachedValue != null) {
                    log.info("Cache rebuilt by another thread for key: {}", cacheKey);
                    return (cachedValue instanceof NullValue) ? null : cachedValue;
                }

                // 执行数据库查询
                Object dbValue = joinPoint.proceed();

                // 回填缓存
                if (dbValue != null) {
                    redisTemplate.opsForValue().set(
                            cacheKey, dbValue, cacheLock.ttl(), cacheLock.unit()
                    );
                    log.info("Cache rebuilt for key: {}", cacheKey);
                } else {
                    redisTemplate.opsForValue().set(
                            cacheKey, NullValue.INSTANCE, 5, TimeUnit.MINUTES
                    );
                    log.warn("DB result is null, caching NullValue for key: {}", cacheKey);
                }
                return dbValue;
            } else {
                // 未拿到锁：直接读缓存，若依然 miss，短暂 sleep 再查一次
                log.warn("Failed to acquire lock for key: {}", cacheKey);
                Thread.sleep(50);
                Object retryValue = redisTemplate.opsForValue().get(cacheKey);
                return (retryValue instanceof NullValue) ? null : retryValue;
            }
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for key: {}", cacheKey);
            }
        }
    }
}
