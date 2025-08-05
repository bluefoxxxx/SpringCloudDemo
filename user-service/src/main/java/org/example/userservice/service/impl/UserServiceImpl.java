package org.example.userservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.userservice.dao.entity.UserDO;
import org.example.userservice.dao.mapper.UserMapper;
import org.example.userservice.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Override
    public Map<String, Object> selectById(Long id, String userIdHeader, String requestIdHeader, String userRole) {
        System.out.println("==================== HEADER VERIFICATION ====================");
        System.out.println("Received call for user ID: " + id);
        System.out.println("Received header [X-User-ID]: " + userIdHeader);
        System.out.println("Received header [X-Request-ID]: " + requestIdHeader);
        System.out.println("Received header [X-User-Role]: " + userRole);
        System.out.println("============================================================");

        Map<String, Object> userMap = new HashMap<>();
        UserDO user = baseMapper.selectById(id);
        userMap.put("id", user.getId());
        userMap.put("name", user.getUsername());
        userMap.put("level", user.getLevel());
        userMap.put("role", userRole);
        return userMap;
    }
}
