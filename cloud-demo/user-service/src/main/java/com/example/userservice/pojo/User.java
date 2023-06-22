package com.example.userservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_user")
public class User {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(value = "username")
    private String username;
    @TableField(value = "address")
    private String address;
}
