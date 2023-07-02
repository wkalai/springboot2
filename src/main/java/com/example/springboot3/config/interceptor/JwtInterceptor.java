package com.example.springboot3.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.springboot3.common.Constants;
import com.example.springboot3.entity.User;
import com.example.springboot3.exception.ServiceException;
import com.example.springboot3.service.IUserService;

import com.example.springboot3.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class JwtInterceptor implements HandlerInterceptor {
    @Autowired

    private IUserService userService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String token =request.getHeader("token"); //从http请求头获取token
        //如果不是映射方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return  true;

        }
        //执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException(Constants.code_401,"无token请重新登录");
        }
        // 获取 token中的userId,根据userId查询数据库，检查用户是否存在
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            throw new ServiceException(Constants.code_401,"token验证失败");
        }
        //根据token中的userid查询数据库
        User user = userService.getById(userId);
        if (user == null) {
            throw new ServiceException(Constants.code_401,"用户不存在，请重新登录");
        }
        // 用户密码加签验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new ServiceException(Constants.code_401,"用户不存在，请重新登录");
        }

        return true;
    }
}
