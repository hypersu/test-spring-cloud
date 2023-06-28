package com.example.orderservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.feignapi.pojo.User;
import lombok.Data;

@Data
@TableName("tb_order")
public class Order {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(value = "price")
    private Long price;
    @TableField(value = "name")
    private String name;
    @TableField(value = "num")
    private Integer num;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(exist = false)
    private User user;
}
