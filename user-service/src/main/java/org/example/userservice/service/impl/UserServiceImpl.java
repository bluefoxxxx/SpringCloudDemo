package org.example.userservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.userservice.dao.entity.UserDO;
import org.example.userservice.dao.mapper.UserMapper;
import org.example.userservice.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient; // 注入 Redisson 客户端

    private static final String CACHE_KEY_PREFIX = "user:";
    private static final String LOCK_KEY_PREFIX = "lock:user:";
    private static final long CACHE_NULL_TTL = 2;
    private static final long CACHE_TTL = 30;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Map<String, Object> selectById(Long id, String userIdHeader, String requestIdHeader, String userRole) {
        System.out.println("==================== HEADER VERIFICATION ====================");
        System.out.println("Received call for user ID: " + id);
        System.out.println("Received header [X-User-ID]: " + userIdHeader);
        System.out.println("Received header [X-Request-ID]: " + requestIdHeader);
        System.out.println("Received header [X-User-Role]: " + userRole);
        System.out.println("============================================================");

        String key = CACHE_KEY_PREFIX + id;

        // 1. 从缓存获取数据
        Object cachedUser = redisTemplate.opsForValue().get(key);
        if (cachedUser != null) {
            if ("null".equals(cachedUser)) {
                log.info("Cache hit for null user: {}", id);
                return null;
            }
            log.info("Cache hit for user: {}", id);
            return (Map<String, Object>) cachedUser;
        }

        // 2. 缓存未命中，使用 Redisson 获取分布式锁
        String lockKey = LOCK_KEY_PREFIX + id;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待时间为0，获取后10秒自动释放，避免死锁
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);

            if (isLocked) {
                log.info("Lock acquired for user: {}", id);
                // 成功获取锁，查询数据库
                log.info("==================== DB QUERY (USER) ====================");
                UserDO user = baseMapper.selectById(id);
                log.info("=========================================================");

                if (user != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getUsername());
                    userMap.put("level", user.getLevel());
                    userMap.put("role", userRole);

                    // 重建缓存
                    redisTemplate.opsForValue().set(key, userMap, CACHE_TTL, TimeUnit.MINUTES);
                    log.info("Cache rebuilt for user: {}", id);
                    return userMap;
                } else {
                    // 解决缓存穿透：数据库中不存在，缓存一个空值
                    redisTemplate.opsForValue().set(key, "null", CACHE_NULL_TTL, TimeUnit.MINUTES);
                    log.warn("User not found in DB, caching null value for ID: {}", id);
                    return null;
                }
            } else {
                // 未获取到锁，休眠后重试
                log.warn("Failed to acquire lock for user: {}, retrying after a short sleep.", id);
                Thread.sleep(50);
                return selectById(id, userIdHeader, requestIdHeader, userRole); // 递归调用
            }
        } catch (InterruptedException e) {
            log.error("Thread was interrupted while trying to acquire lock or sleep.", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            return null;
        } finally {
            // 确保释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for user: {}", id);
            }
        }
    }
}
