package com.blog.log.annotation;

import com.blog.log.enums.BusinessType;
import com.blog.log.enums.OperatorType;

import java.lang.annotation.*;

/**
 * @author lxk
 * @description 自定义日志注解
 * @date 2025/01/19
 */

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log
{
    /**
     * 模块
     */
    public String module() default "";

    /**
     * 业务
     */
    public String business() default "";

    /**
     * 操作
     * @return
     */
    public String operate() default "";

    /**
     * 模块
     */
    public String title() default "";

    /**
     * 功能
     */
    public BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
    public OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
    public boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    public boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数
     */
    public String[] excludeParamNames() default {};

    /**
     * 是否保存到数据库
     */
    public boolean isSaveDataBase() default true;

    /**
     * 过滤耗时
     */
    public long filterCostTime() default 0L;
}

