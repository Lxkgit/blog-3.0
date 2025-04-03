package com.blog.log.annotation;

import com.blog.log.enums.BusinessType;
import com.blog.log.enums.OperatorType;

import java.lang.annotation.*;

/**
 * @author lxk
 * @description 自定义日志注解
 * @date 2025/01/19
 */


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Log {

    /**
     * 模块
     */
    String module() default "";

    /**
     * 业务
     */
    String business() default "";

    /**
     * 操作
     *
     * @return
     */
    String operate() default "";

    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
    OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数
     */
    String[] excludeParamNames() default {};

    /**
     * 是否保存到数据库
     */
    boolean isSaveDataBase() default true;

    /**
     * 过滤耗时
     */
    long filterCostTime() default 0L;
}

