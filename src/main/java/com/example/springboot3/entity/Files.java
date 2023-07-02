package com.example.springboot3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_file")
public class Files {
    @TableId(type = IdType.AUTO)
    private  Integer id;
    private  String name;
    private  String type;
    private  String url;
    private  String md5;
    private  Long size;
    private Boolean isDelete;
    private Boolean enable;
}
