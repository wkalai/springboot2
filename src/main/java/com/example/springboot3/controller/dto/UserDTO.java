package com.example.springboot3.controller.dto;

import lombok.Data;
//接受前端请求参数

@Data

public class UserDTO {
    private  String username;
    private  String password;
    private  String nickname;
    private String avatarUrl;
    private  String token;
}
