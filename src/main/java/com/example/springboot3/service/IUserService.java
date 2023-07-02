package com.example.springboot3.service;

import com.example.springboot3.controller.dto.UserDTO;
import com.example.springboot3.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LJ
 * @since 2023-06-03
 */
public interface IUserService extends IService<User> {

    UserDTO login(UserDTO userDTO);

  User register(UserDTO userDTO);
}
