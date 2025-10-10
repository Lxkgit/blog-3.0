package com.blog.core.exception;

import com.blog.core.constant.ErrorConstant;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-08-14
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获服务处理异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e) {
        logger.error(e.getMessage(), e);
        if (StringUtils.isNotEmpty(e.getCode())) {
            return ResultFactory.buildFailResult(e.getCode(), e.getErrMsg());
        }
        return ResultFactory.buildFailResult(e.getErrMsg());
    }

    /**
     * 捕获代码执行异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        logger.error(e.getMessage(), e);
        return ResultFactory.buildFailResult(ErrorConstant.UNKNOWN_ERROR.getCode(), "系统内部错误: " + e.getMessage());
    }
}
