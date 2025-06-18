package com.blog.pi.netty.dto.heart;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

/**
 * @description: 心跳消息类
 * @Author: lxk
 * @date 2024/3/14 15:12
 */

@Data
public class NettyHeartBeatDto {

    /**
     * 心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date heartBeat;

    /**
     * 心跳类型
     * 1： 树莓派等服务器设备
     */
    private Integer type;

    /**
     * mqtt上注册的单片机id
     */
    private List<String> clientIds;

}
