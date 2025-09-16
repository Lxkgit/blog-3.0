package com.blog.file.netty.listener;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettyUploadBlogFileDto;
import com.blog.file.netty.domain.dto.register.NettyRegisterDto;
import com.blog.file.netty.domain.enums.NettyPacketType;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.netty.domain.enums.NettyTopicEnum;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.netty.event.NettyPacketEvent;
import com.blog.file.netty.service.*;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.channel.ChannelId;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @description: Netty服务端自定义数据包处理监听器
 * @Author: lxk
 * @date 2024/1/6 15:17
 */
@Component
@RequiredArgsConstructor
public class NettyServerPacketListener implements ApplicationListener<NettyPacketEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerPacketListener.class);


    private final NettyServer nettyServer;

    @Resource
    private DeviceMapper deviceDAO;

    @Resource
    private NettyDeviceService nettyDeviceData;

    @Resource
    private NettyFileSyncService nettyFileSyncService;

    @Resource
    private UserDeviceMapper userDeviceDAO;

    @Resource
    private NettyServerHandler nettyServerHandler;

    @Resource
    private RedisService redisService;

    @SneakyThrows
    @Async
    @Override
    public void onApplicationEvent(NettyPacketEvent event) {
        ChannelId channelId = (ChannelId) event.getSource();
        String nettyPacketType = event.getNettyPacket().getNettyPacketType();
        String requestId = event.getNettyPacket().getRequestId();
        String topic = event.getNettyPacket().getTopic();

        String registerCode = event.getNettyPacket().getRegisterCode();
        Integer userId = Integer.parseInt(registerCode.split(":")[0]);
        String deviceCode = registerCode.split(":")[1];
        String data = event.getNettyPacket().getData().toString();

        if (!nettyPacketType.equals(NettyPacketType.HEARTBEAT.getValue())) {
            // 只输出非心跳消息
            logger.info("netty 收到消息，channelId: {} requestId: {} nettyPacketType: {} topic: {} deviceCode: {} data: {}",
                    channelId, requestId, nettyPacketType, topic, deviceCode, data);
        }
        if (!nettyServerHandler.checkContainByDeviceCode(deviceCode)) {
            nettyServerHandler.closeChannelByDeviceCode(deviceCode);
        }
        if (nettyPacketType.equals(NettyPacketType.REGISTER.getValue())) {
            // netty 通道注册
            deviceRegister(userId, deviceCode, channelId, data);
        } else if (nettyPacketType.equals(NettyPacketType.HEARTBEAT.getValue())) {
            // 心跳消息 收到消息设置设备在线3分钟
            redisService.setString(FileRedisConstant.FILE_DEVICE_STATUS + deviceCode, data, 180);
        } else if (nettyPacketType.equals(NettyPacketType.REQUEST.getValue())) {
            // 处理单片机、传感器注册数据
            if (topic.equals(NettyTopicEnum.CHIP_SENSOR_REGISTER.getTopic())) {
                nettyDeviceData.chipAndSensorRegister(data, deviceCode, 1);
            } else if (topic.equals(NettyTopicEnum.SENSOR_DATA.getTopic())) {
                nettyDeviceData.receiveSensorData(data, deviceCode);
            } else if (topic.equals(NettyTopicEnum.DEVICE_INFO.getTopic())) {
                nettyDeviceData.deviceInfo(data, deviceCode, userId);
            }
            // 回复请求消息响应
            NettyPacket<String> nettyResponse = NettyPacket.buildResponse(requestId, "response");
            nettyResponse.setTopic(topic);
            nettyServer.sendByRegisterIdNotRetry(registerCode, JSONObject.toJSONString(nettyResponse));
        } else if (nettyPacketType.equals(NettyPacketType.RESPONSE.getValue())) {
            if (NettyTopic.BLOG_FILE_SYNC.equals(topic)) {
                JSONObject jsonObject = JSONObject.parseObject(data);
                NettyUploadBlogFileDto nettyUploadBlogFileDto = JSONObject.parseObject(jsonObject.getString("message"), NettyUploadBlogFileDto.class);
                nettyFileSyncService.syncFileReceive(nettyUploadBlogFileDto, deviceCode, userId);
            }

            // 接收响应
            if (topic.equals(NettyTopicEnum.MSG_ERROR_RESPONSE.getTopic())) {
                // 处理发送异常的消息
            }


        }
    }

    /**
     * pi服务部署设备注册流程
     *
     * @param userId     所属用户id
     * @param deviceCode 设备编码
     * @param channelId  netty通道
     * @param data       设备注册数据
     */
    private void deviceRegister(Integer userId, String deviceCode, ChannelId channelId, String data) {
        // netty设备注册 单片机 传感器注册流程
        QueryWrapper<UserDevice> userDeviceQueryWrapper = new QueryWrapper<>();
        userDeviceQueryWrapper.eq("user_id", userId).eq("device_code", deviceCode);
        UserDevice selectDevice = userDeviceDAO.selectOne(userDeviceQueryWrapper);
        // 设备编码错误拒绝注册
        if (selectDevice == null) {
            logger.error("用户与编码匹配失败，拒绝连接");
            nettyServerHandler.closeChannelByChannelId(channelId);
        } else {
            // netty 设备通道绑定 后续发送消息获取通道
            NettyRegisterDto nettyRegisterDto = JSONObject.parseObject(data, NettyRegisterDto.class);
            if (!NettyServerHandler.clientMap.containsKey(deviceCode)) {
                addNettyChannel(channelId, deviceCode);
                logger.info("netty 通道注册 register: deviceCode:{} channelId:{}", deviceCode, channelId);
            }

            // 创建设备 写入数据
            Device device = new Device();
            device.setUserId(1);
            device.setDeviceName(nettyRegisterDto.getDeviceName());
            device.setDeviceCode(deviceCode);
            device.setDataJson(JSONObject.toJSONString(data));
            device.setUpdateTime(new Date());
            device.setDeviceStatus(1);
            device.setMemo(nettyRegisterDto.getMemo());

            // 当前设备未注册 首次注册创建设备
            if (selectDevice.getCodeStatus() == 0) {
                device.setCreateTime(new Date());
                deviceDAO.insert(device);

                // 将设备状态修改为已注册
                UserDevice userDevice = new UserDevice();
                userDevice.setId(selectDevice.getId());
                userDevice.setCodeStatus(1);
                userDevice.setUpdateTime(new Date());
                userDeviceDAO.updateById(userDevice);
            } else {

                // 当前设备已注册 更新设备数据
                QueryWrapper<Device> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", userId).eq("device_code", deviceCode);
                deviceDAO.update(device, wrapper);
            }
        }
    }

    /**
     * @param channelId
     * @param deviceCode
     */
    private void addNettyChannel(ChannelId channelId, String deviceCode) {
        NettyServerHandler.clientMap.put(deviceCode, channelId);
    }

}

