package com.example.demo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String name;
    public String email;
    public Status status;
    public LocalDateTime createdAt;

    public enum Status { ACTIVE, DISABLED }
}
