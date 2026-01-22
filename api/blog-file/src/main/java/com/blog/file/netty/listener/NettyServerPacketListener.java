package com.blog.file.netty.listener;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.dto.register.NettyRegisterDto;
import com.blog.file.netty.domain.enums.NettyPacketType;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.netty.domain.enums.NettyTopicEnum;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.netty.event.NettyPacketEvent;
import com.blog.file.netty.service.*;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.constant.NettyRedisConstant;
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
    private DeviceMapper deviceMapper;

    @Resource
    private NettyDeviceService nettyDeviceData;

    @Resource
    private NettySyncFileService nettyFileSyncService;

    @Resource
    private UserDeviceMapper userDeviceDAO;

    @Resource
    private NettyServerHandler nettyServerHandler;

    @Resource
    private RedisService redisService;

//    @SneakyThrows
    @Async
    @Override
    public void onApplicationEvent(NettyPacketEvent event) {
        ChannelId channelId = (ChannelId) event.getSource();
        MsgHead msgHead = event.getNettyPacket().getMsgHead();

        String nettyPacketType = msgHead.getNettyMsgHead().getNettyPacketType();
        String requestId = msgHead.getNettyMsgHead().getRequestId();
        String topic = msgHead.getNettyMsgHead().getTopic();
        String registerCode = msgHead.getNettyMsgHead().getRegisterCode();

        Integer userId = Integer.parseInt(registerCode.split(":")[0]);
        String deviceCode = registerCode.split(":")[1];
        String data = event.getNettyPacket().getData().toString();
        if (NettyPacketType.HEARTBEAT.getValue().equals(nettyPacketType)) {
            logger.info("===== netty 心跳 ===== registerCode: {} data: {}", registerCode, data);
        } else {
            logger.info("===== netty 收到消息 ===== msgHead: {} channelId: {} requestId: {} nettyPacketType: {} topic: {} deviceCode: {} data: {}",
                    msgHead, channelId, requestId, nettyPacketType, topic, deviceCode, data);
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
            // 回复请求消息响应(业务内部可以会再次响应消息，此响应防止客户端重发消息)
            NettyPacket<String> nettyResponse = NettyPacket.buildResponse(requestId, topic, "response");
            nettyServer.sendByRegisterIdNotRetry(registerCode, JSONObject.toJSONString(nettyResponse));

            // 处理单片机、传感器注册数据
            if (topic.equals(NettyTopicEnum.CHIP_SENSOR_REGISTER.getTopic())) {
                nettyDeviceData.chipAndSensorRegister(data, deviceCode, userId);
            } else if (topic.equals(NettyTopicEnum.SENSOR_DATA.getTopic())) {
                nettyDeviceData.receiveSensorData(data, deviceCode);
            } else if (topic.equals(NettyTopicEnum.DEVICE_INFO.getTopic())) {
                nettyDeviceData.deviceInfo(data, deviceCode, userId);
            }
        } else if (nettyPacketType.equals(NettyPacketType.RESPONSE.getValue())) {
            // 记录响应类消息记录消息序列号，取消对此消息重发
            logger.info("消息 requestId：{} 收到响应", requestId);
            redisService.setSet(NettyRedisConstant.NETTY_RECEIVE_QUEUE, requestId);

            if (NettyTopic.BLOG_FILE_SYNC.equals(topic)) {
                // 文件同步上传响应数据处理
                JSONObject jsonObject = JSONObject.parseObject(data);
                NettySyncFileDto nettyFileSyncDto = JSONObject.parseObject(jsonObject.getString("message"), NettySyncFileDto.class);
                nettyFileSyncService.receiveSyncFileMsg(msgHead, nettyFileSyncDto);
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
            logger.error("deviceRegister 异常断开 netty 通道连接: 用户与编码匹配失败，拒绝连接");
            nettyServerHandler.closeChannelByChannelId(channelId);
        } else {
            // netty 设备通道绑定 后续发送消息获取通道
            NettyRegisterDto nettyRegisterDto = JSONObject.parseObject(data, NettyRegisterDto.class);
            if (!NettyServerHandler.CLIENT_MAP.containsKey(deviceCode)) {
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
                deviceMapper.insert(device);

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
                deviceMapper.update(device, wrapper);
            }
        }
    }

    /**
     * @param channelId
     * @param deviceCode
     */
    private void addNettyChannel(ChannelId channelId, String deviceCode) {
        NettyServerHandler.CLIENT_MAP.put(deviceCode, channelId);
    }

}

