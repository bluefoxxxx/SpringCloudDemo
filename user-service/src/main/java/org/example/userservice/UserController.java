package org.example.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // 使用 @RequestHeader 注解直接获取特定的请求头
    @GetMapping("/users/{id}")
    public Map<String, Object> getUserById(
            @PathVariable("id") Long id,
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            @RequestHeader(name = "X-Request-ID", required = false) String requestIdHeader) {

        log.info("成功接收到获取用户信息的请求, 用户ID: {}", id);

        System.out.println("==================== HEADER VERIFICATION ====================");
        System.out.println("Received call for user ID: " + id);
        System.out.println("Received header [X-User-ID]: " + userIdHeader);
        System.out.println("Received header [X-Request-ID]: " + requestIdHeader);
        System.out.println("============================================================");

        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", "张三");
        user.put("level", "黄金会员");
        return user;
    }
}
