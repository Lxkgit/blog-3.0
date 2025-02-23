package com.dmg.authserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public Result usernameNotFoundException(UsernameNotFoundException e) {
        return Result.error("401",e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result badCredentialsException(BadCredentialsException e) {
        return Result.error("401",e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result exception(Exception e) {
        return Result.error("500",e.getMessage());
    }
}
