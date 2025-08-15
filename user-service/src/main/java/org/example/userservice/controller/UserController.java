package org.example.userservice.controller;

import org.example.userservice.convention.result.Result;
import org.example.userservice.convention.result.Results;
import org.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // 使用 @RequestHeader 注解直接获取特定的请求头
    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserById(
            @PathVariable("id") Long id,
            @RequestHeader(name = "X-User-ID", required = false) String userIdHeader,
            @RequestHeader(name = "X-Request-ID", required = false) String requestIdHeader,
            @RequestHeader(name = "X-User-Role", required = false) String userRole) {
        Map<String, Object> user = userService.selectById(id, userIdHeader, requestIdHeader, userRole);
        return Results.success(user);
    }

    // 新增 v2 接口
    @GetMapping("/v2/users/{id}")
    public Result<Map<String, Object>> getUserByIdV2(@PathVariable("id") Long id) {
        log.info("V2 - 成功接收到获取用户信息的请求, 用户ID: {}", id);

        Map<String, Object> user = new HashMap<>();
        user.put("userId", id);
        user.put("username", "张三(VIP)");
        user.put("membershipLevel", "PLATINUM");
        user.put("version", "v2");
        return Results.success(user);
    }
}
