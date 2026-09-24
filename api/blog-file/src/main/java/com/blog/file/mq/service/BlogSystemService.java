package com.blog.file.mq.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.ip.entity.IpLocation;
import com.blog.core.utils.IpUtil;
import com.blog.file.mapper.IpLocationMapper;
import com.blog.file.mq.MqMessageHandlerService;
import com.blog.mq.entity.MqMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @Description MQ博客系统消息处理
 * @Author lxk
 * @CreateTime 2026-09-24
 */

@Service
public class BlogSystemService {

    private static final Logger logger = LoggerFactory.getLogger(BlogSystemService.class);

    private static final List<String> ERROR_IP_LIST = Arrays.asList("127.0.0.1", "localhost");

    @Resource
    private IpLocationMapper ipLocationMapper;

    public void getIpLocation(MqMessage mqMessage) {
        JSONObject message = JSONObject.parseObject(mqMessage.getMessage());
        String ip = message.getString("ip");

        if (ERROR_IP_LIST.contains(ip)) {
            logger.error("定位ip异常： {}", ip);
            return;
        }

        LambdaQueryWrapper<IpLocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IpLocation::getIp, ip);
        List<IpLocation> ipLocationList = ipLocationMapper.selectList(queryWrapper);
        if (!ipLocationList.isEmpty()) {
            logger.error("ip 已有定位数据： {}", ip);
            return;
        }

        JSONObject result = IpUtil.getIpLocation(ip);

        IpLocation ipLocation = new IpLocation();
        ipLocation.setIp(ip);
        ipLocation.setUserId(message.getInteger("userId"));

        ipLocation.setCountry(result.getString("country"));
        ipLocation.setCountryCode(result.getString("countryCode"));
        ipLocation.setRegion(result.getString("region"));
        ipLocation.setCity(result.getString("city"));
        ipLocation.setLat(result.getBigDecimal("lat"));
        ipLocation.setLon(result.getBigDecimal("lon"));
        ipLocation.setIsp(result.getString("isp"));
        ipLocationMapper.insert(ipLocation);
    }

}
