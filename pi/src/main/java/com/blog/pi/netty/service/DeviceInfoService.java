package com.blog.pi.netty.service;

import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import com.blog.pi.netty.dto.heart.NettyHeartBeatDto;
import com.blog.pi.netty.dto.register.NettyRegisterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @Description 设备信息服务
 * @Author lxk
 * @CreateTime 2024-08-28
 */

@Slf4j
@Service
public class DeviceInfoService {

    /**
     * 注册获取设备信息
     * @param registerDto
     */
    public void setRegisterMsg(NettyRegisterDto registerDto) {


    }

    /**
     * 心跳上报获取设备信息
     * @param heartBeatDto
     */
    public void setHeartBeatMsg(NettyHeartBeatDto heartBeatDto) {
        try {
//            heartBeatDto.setCpuInfo(cpuInfo());
//            heartBeatDto.setMemInfo(memInfo());
//            heartBeatDto.setNetInfo(net());
//            heartBeatDto.setSysFile(sysFiles());
        } catch (Exception e) {
            log.error("获取设备信息失败：" + e.getMessage());
            e.printStackTrace();
        }

    }

}
