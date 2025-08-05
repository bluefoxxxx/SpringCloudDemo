package org.example.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.userservice.dao.entity.UserDO;

import java.util.Map;

public interface UserService extends IService<UserDO> {

    Map<String, Object> selectById(Long id, String userIdHeader, String requestIdHeader, String userRole);
}
