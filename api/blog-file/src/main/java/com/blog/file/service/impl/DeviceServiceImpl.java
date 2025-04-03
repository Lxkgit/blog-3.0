package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.constant.ErrorMessage;
import com.blog.core.domain.file.device.entity.Chip;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.entity.DeviceHeartbeat;
import com.blog.core.domain.file.device.vo.DeviceHeartbeatVo;
import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.ChipMapper;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.mapper.DeviceHeartbeatMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.service.DeviceService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description: 下级设备服务
 * @Author: lxk
 * @date 2024/1/29 13:50
 */

@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceDAO;

    @Resource
    private ChipMapper chipDAO;

    @Resource
    private UserDeviceMapper userDeviceDAO;

    @Resource
    private DeviceHeartbeatMapper deviceHeartbeatDAO;

    /**
     * 新增设备
     *
     * @param userId
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer addDevice(Integer userId, DeviceVo deviceVo) throws ServiceException {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("device_code", deviceVo.getDeviceCode());
        Device device = deviceDAO.selectOne(wrapper);
        if (device != null) {
            throw new ServiceException(ErrorMessage.DEVICE_CODE_EXISTS);
        }
//        BlogUser blogUser = JSONObject.parseObject(JSONObject.toJSONString(userClient.getUserById(userId).getResult()), BlogUser.class);
//        deviceVo.setUserId(blogUser.getId());
        deviceVo.setDeviceStatus(Constant.DEVICE_OFFLINE);
        deviceVo.setCreateTime(new Date());
        deviceVo.setUpdateTime(new Date());
        deviceDAO.insert(deviceVo);
        return deviceVo.getId();
    }

    /**
     * 删除设备
     *
     * @param userId
     * @param ids
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer deleteDevice(Integer userId, String ids) throws ServiceException {
        Set<String> idSet = MyStringUtils.splitString(ids, ",");
        for (String id : idSet) {
            Device device = deviceDAO.selectById(Integer.parseInt(id));
            if (device != null) {
//                DeviceStatusSchedule.removeChannelByRegisterId(device.getDeviceCode(), deviceDAO);
                deviceDAO.updateDeviceStatusById(id, userId, Constant.DEVICE_DELETE);
            } else {
                throw new ServiceException(ErrorMessage.DEVICE_NOT_EXISTS, "id: " + id);
            }
        }
        return idSet.size();
    }

    /**
     * 修改设备
     *
     * @param userId
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer updateDevice(Integer userId, DeviceVo deviceVo) throws ServiceException {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("device_code", deviceVo.getDeviceCode());
        wrapper.ne("id", deviceVo.getId());
        Device device = deviceDAO.selectOne(wrapper);
        if (device != null) {
            throw new ServiceException(ErrorMessage.DEVICE_CODE_EXISTS);
        }
        Device oldDevice = deviceDAO.selectById(deviceVo.getId());
        // 设备编码变化需要重新连接netty通道
        if (!oldDevice.getDeviceCode().equals(deviceVo.getDeviceCode())) {
//            DeviceStatusSchedule.removeChannelByRegisterId(oldDevice.getDeviceCode(), deviceDAO);
        }
        deviceDAO.updateById(deviceVo);
        return deviceVo.getId();
    }

    /**
     * 查询设备列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<Device> selectDeviceList(Integer userId) throws ServiceException {
//        BlogUser blogUser = userService.getBlogUserById(userId);
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getUserId, 1);
        return deviceDAO.selectList(wrapper);
    }


    @Override
    public DeviceVo selectDeviceById(Integer userId, Integer id) throws ServiceException {

//        BlogUser blogUser = userService.getBlogUserById(userId);

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getId, id);
        wrapper.eq(Device::getUserId, 1);
        Device device = deviceDAO.selectOne(wrapper);

        DeviceVo deviceVo = new DeviceVo();
        BeanUtils.copyProperties(device, deviceVo);

        LambdaQueryWrapper<Chip> chipLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chipLambdaQueryWrapper.eq(Chip::getDeviceCode, device.getDeviceCode());
        List<Chip> chipList = chipDAO.selectList(chipLambdaQueryWrapper);
        deviceVo.setChipList(chipList);

        return deviceVo;
    }

    /**
     * 查询设备详细信息
     * @param userId 用户id
     * @param id 设备id
     * @return
     */
    @Override
    public List<DeviceHeartbeatVo> selectDeviceInfoById(Integer userId, Integer id) {

        int dataCount = 100;

        Device device = deviceDAO.selectById(id);

        LambdaQueryWrapper<DeviceHeartbeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceHeartbeat::getUserId, userId);
        wrapper.eq(DeviceHeartbeat::getDeviceCode, device.getDeviceCode());
        wrapper.orderByDesc(DeviceHeartbeat::getId);
        wrapper.last("LIMIT " + dataCount);

        List<DeviceHeartbeat> list = deviceHeartbeatDAO.selectList(wrapper);

        List<DeviceHeartbeatVo> voList = new ArrayList<>();

        list.forEach(item -> {
            DeviceHeartbeatVo vo = new DeviceHeartbeatVo();
            BeanUtils.copyProperties(item, vo);
//            vo.setNettyHeartBeatDto(JSONObject.parseObject(item.getDeviceJson(), NettyHeartBeatDto.class));
            vo.setDeviceJson(null);
            voList.add(vo);
        });

        return voList;
    }
}
