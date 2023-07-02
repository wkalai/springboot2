package com.example.springboot3.exception;

import com.example.springboot3.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice

    public class GlobalExceptionHandler {


    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handle(ServiceException se) {

        return Result.error(se.getCode(), se.getMessage());
    }
}

