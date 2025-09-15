package com.blog.core.domain.file.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description 传感器模板数据类
 * @Author lxk
 * @CreateTime 2024-09-29
 */

@Data
@TableName("blog_sensor_template")
public class SensorTemplate {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 传感器类型
     */
    private String sensorType;

    /**
     * 模板内容
     */
    private String template;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最近修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
