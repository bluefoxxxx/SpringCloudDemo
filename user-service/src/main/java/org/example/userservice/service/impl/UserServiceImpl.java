package org.example.userservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.userservice.aop.CacheLock;
import org.example.userservice.convention.exception.ClientException;
import org.example.userservice.dao.entity.UserDO;
import org.example.userservice.dao.mapper.UserMapper;
import org.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @CacheLock(prefix = "user:", ttl = 30, unit = TimeUnit.MINUTES) // 核心改动点！
    public Map<String, Object> selectById(Long id, String userIdHeader, String requestIdHeader, String userRole) {
        // AOP 将缓存和锁的逻辑透明地织入，业务代码只剩下最核心的数据库查询
        log.info("==================== DB QUERY (USER) via AOP ====================");
        UserDO user = baseMapper.selectById(id);
        log.info("=================================================================");

        // 用户不存在，抛出客户端异常
        if (user == null) {
            throw new ClientException("用户不存在");
        }

        // 业务逻辑：将 DO 转换为 Map DTO
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getUsername());
        userMap.put("level", user.getLevel());
        // 仍然可以从参数中获取 Header 信息并加入返回结果
        userMap.put("role", userRole);

        System.out.println("==================== HEADER VERIFICATION ====================");
        System.out.println("Received call for user ID: " + id);
        System.out.println("Received header [X-User-ID]: " + userIdHeader);
        System.out.println("Received header [X-Request-ID]: " + requestIdHeader);
        System.out.println("Received header [X-User-Role]: " + userRole);
        System.out.println("============================================================");

        return userMap;
    }
}

