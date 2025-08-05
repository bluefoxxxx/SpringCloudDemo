package org.example.userservice.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_user")
public class UserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String level;
}
