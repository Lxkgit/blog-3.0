package com.blog.core.utils.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lxk
 * @description
 * @date 2025/07/16
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelValueMap {

    /**
     * 映射关系配置（格式：数字:文本, 数字:文本）
     * 示例：1:是,0:否
     */
    String valueMapping() default "";

    /**
     * 读取时的默认值（当文本不在映射中时使用）
     */
    int readDefault() default -1;

    /**
     * 写入时的默认文本（当数字不在映射中时使用）
     */
    String writeDefault() default "";
}
