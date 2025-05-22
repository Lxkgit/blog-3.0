package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.constant.ErrorMessage;
import com.blog.core.domain.file.device.entity.Chip;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.entity.DeviceInfo;
import com.blog.core.domain.file.device.vo.DeviceInfoVo;
import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.ChipMapper;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.mapper.DeviceInfoMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.netty.schedule.DeviceStatusSchedule;
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
    private DeviceMapper deviceMapper;

    @Resource
    private ChipMapper chipMapper;

    @Resource
    private UserDeviceMapper userDeviceDAO;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    /**
     * 新增设备
     *
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer addDevice(DeviceVo deviceVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("device_code", deviceVo.getDeviceCode());
        Device device = deviceMapper.selectOne(wrapper);
        if (device != null) {
            throw new ServiceException(ErrorMessage.DEVICE_CODE_EXISTS);
        }
        deviceVo.setUserId(userId);
        deviceVo.setDeviceStatus(Constant.DEVICE_OFFLINE);
        deviceVo.setCreateTime(new Date());
        deviceVo.setUpdateTime(new Date());
        deviceMapper.insert(deviceVo);
        return deviceVo.getId();
    }

    /**
     * 删除设备
     *
     * @param ids
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer deleteDevice(String ids) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        Set<String> idSet = MyStringUtils.splitString(ids, ",");
        for (String id : idSet) {
            Device device = deviceMapper.selectById(Integer.parseInt(id));
            if (device != null) {
                DeviceStatusSchedule.removeChannelByRegisterId(device.getDeviceCode(), deviceMapper);
                deviceMapper.updateDeviceStatusById(id, userId, Constant.DEVICE_DELETE);
            } else {
                throw new ServiceException(ErrorMessage.DEVICE_NOT_EXISTS, "id: " + id);
            }
        }
        return idSet.size();
    }

    /**
     * 修改设备
     *
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer updateDevice(DeviceVo deviceVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("device_code", deviceVo.getDeviceCode());
        wrapper.ne("id", deviceVo.getId());
        Device device = deviceMapper.selectOne(wrapper);
        if (device != null) {
            throw new ServiceException(ErrorMessage.DEVICE_CODE_EXISTS);
        }
        Device oldDevice = deviceMapper.selectById(deviceVo.getId());
        // 设备编码变化需要重新连接netty通道
        if (!oldDevice.getDeviceCode().equals(deviceVo.getDeviceCode())) {
            DeviceStatusSchedule.removeChannelByRegisterId(oldDevice.getDeviceCode(), deviceMapper);
        }
        deviceMapper.updateById(deviceVo);
        return deviceVo.getId();
    }

    /**
     * 查询设备列表
     *
     * @return
     */
    @Override
    public List<Device> selectDeviceList() throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getUserId, userId);
        return deviceMapper.selectList(wrapper);
    }


    @Override
    public DeviceVo selectDeviceById(Integer id) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getId, id);
        wrapper.eq(Device::getUserId, userId);
        Device device = deviceMapper.selectOne(wrapper);

        DeviceVo deviceVo = new DeviceVo();
        BeanUtils.copyProperties(device, deviceVo);

        LambdaQueryWrapper<Chip> chipLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chipLambdaQueryWrapper.eq(Chip::getDeviceCode, device.getDeviceCode());
        List<Chip> chipList = chipMapper.selectList(chipLambdaQueryWrapper);
        deviceVo.setChipList(chipList);

        return deviceVo;
    }

    /**
     * 查询设备详细信息
     * @param id 设备id
     * @return
     */
    @Override
    public List<DeviceInfoVo> selectDeviceInfoById(Integer id) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        int dataCount = 100;

        Device device = deviceMapper.selectById(id);

        LambdaQueryWrapper<DeviceInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceInfo::getUserId, userId);
        wrapper.eq(DeviceInfo::getDeviceCode, device.getDeviceCode());
        wrapper.orderByDesc(DeviceInfo::getId);
        wrapper.last("LIMIT " + dataCount);

        List<DeviceInfo> list = deviceInfoMapper.selectList(wrapper);

        List<DeviceInfoVo> voList = new ArrayList<>();

        list.forEach(item -> {
            DeviceInfoVo vo = new DeviceInfoVo();
            BeanUtils.copyProperties(item, vo);
//            vo.setNettyHeartbeatDto(JSONObject.parseObject(item.getDeviceJson(), NettyHeartbeatDto.class));
            vo.setDeviceJson(null);
            voList.add(vo);
        });

        return voList;
    }
}
