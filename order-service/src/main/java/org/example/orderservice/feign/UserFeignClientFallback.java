package org.example.orderservice.feign;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public Map<String, Object> getUserById(Long id) {
        // 降级逻辑，当用户服务不可用时，返回一个默认的、友好的用户信息
        Map<String, Object> fallbackUser = new HashMap<>();
        fallbackUser.put("id", id);
        fallbackUser.put("name", "默认用户(服务降级)");
        fallbackUser.put("level", "N/A");
        fallbackUser.put("error", "用户服务繁忙，请稍后再试");
        return fallbackUser;
    }
}
