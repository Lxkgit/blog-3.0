package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.domain.netty.dto.NettyPacket;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.core.domain.netty.enums.NettyTopic;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.file.mapper.UserDeviceMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-07-27
 */

@Service
public class NettySyncFileSendService {

    @Resource
    private NettyServer nettyServer;

    @Resource
    private UserDeviceMapper userDeviceMapper;

    /**
     * 发送文件同步消息至树莓派
     *
     * @param nettySyncFileDto 同步文件参数
     */
    public boolean sendSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto, Integer userId) {

        // 获取用户默认同步数据设备
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDevice::getUserId, userId);
        List<UserDevice> deviceList = userDeviceMapper.selectList(wrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            NettyPacket<NettySyncFileDto> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, nettySyncFileDto);
            nettyPacket.setMsgHead(msgHead);
            return nettyServer.sendByRegisterIdLimitTime(registerId, nettyPacket.getMsgHead().getNettyMsgHead().getRequestId(),
                    JSON.toJSONString(nettyPacket), 2 * 60);

        }
        return false;
    }
}
