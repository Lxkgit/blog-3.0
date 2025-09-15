package com.blog.core.domain.file.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description 设备单片机对应表
 * @Author lxk
 * @CreateTime 2024-08-31
 */

@Data
@TableName("device_chip")
public class DeviceChip {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 单片机编码
     */
    private String chipCode;

    /**
     * 设备编码状态
     */
    private Integer codeStatus;

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
