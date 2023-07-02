package com.example.springboot3.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot3.common.Constants;
import com.example.springboot3.controller.dto.UserDTO;
import com.example.springboot3.entity.User;
import com.example.springboot3.exception.ServiceException;
import com.example.springboot3.mapper.UserMapper;
import com.example.springboot3.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import jdk.jpackage.internal.Log;
import com.example.springboot3.utils.TokenUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LJ
 * @since 2023-06-03
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public UserDTO login(UserDTO userDTO) {
    User one=  getUserInfo(userDTO);

        if (one != null) {
            BeanUtil.copyProperties(one, userDTO, true);
            //设置token
           String token= TokenUtils.genToken(one.getId().toString(),one.getPassword());
           userDTO.setToken(token);

            return userDTO;
        } else {
            throw new ServiceException(Constants.code_600, "用户名或者密码错误");
        }

    }

    @Override
    public User register(UserDTO userDTO) {
        User one=  getUserInfo(userDTO);
        if(one==null){
            one=new User();
            BeanUtil.copyProperties(userDTO, one, true);

            save(one);//把copy完成的用户数据保存到数据库中

        }else {
            throw new ServiceException(Constants.code_600, "用户已存在");
        }

        return one;
    }

    private User getUserInfo(UserDTO userDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());
        User one;
        try {
            one = getOne(queryWrapper);

        } catch (Exception e) {
            throw new ServiceException(Constants.code_500, "系统错误");

        }
        return  one;

    }


}
