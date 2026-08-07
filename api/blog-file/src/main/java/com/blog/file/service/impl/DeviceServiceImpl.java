package com.blog.file.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.constant.ErrorConstant;
import com.blog.core.domain.file.device.entity.Chip;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.entity.DeviceInfo;
import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.ChipMapper;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.mapper.DeviceInfoMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.service.DeviceService;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Resource
    private RedisService redisService;

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
            throw new ServiceException(ErrorConstant.DEVICE_CODE_EXISTS);
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
//                DeviceStatusSchedule.removeChannelByRegisterId(device.getDeviceCode(), deviceMapper);
//                deviceMapper.updateDeviceStatusById(id, userId, Constant.DEVICE_DELETE);
            } else {
                throw new ServiceException(ErrorConstant.DEVICE_NOT_EXISTS, "id: " + id);
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
            throw new ServiceException(ErrorConstant.DEVICE_CODE_EXISTS);
        }
//        Device oldDevice = deviceMapper.selectById(deviceVo.getId());
//        // 设备编码变化需要重新连接netty通道
//        if (!oldDevice.getDeviceCode().equals(deviceVo.getDeviceCode())) {
//            DeviceStatusSchedule.removeChannelByRegisterId(oldDevice.getDeviceCode(), deviceMapper);
//        }
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
        List<Device> deviceList = deviceMapper.selectList(wrapper);
        for (Device device : deviceList) {
            device.setDeviceStatus(0);
            if (redisService.hasKey(FileRedisConstant.FILE_DEVICE_STATUS + device.getDeviceCode())) {
                device.setDeviceStatus(1);
            }
        }
        return deviceList;
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

    @Override
    public void getDeviceStatus() {

    }

    /**
     * 数据格式：
     * {"system":"Windows","node_name":"DESKTOP-DQRP5MD","machine":"AMD64",
     * "cpu":{"physical_cores":4,"usage_per_core":[0.0,0.0,0.0,0.0],"total_usage":10.7},
     * "memory":{"total":16999424000,"available":3872051200,"used":13127372800,"percent":77.2},
     * "disks":[{"device":"C:\\","mountpoint":"C:\\","fstype":"NTFS","opts":"rw,fixed","usage":{"total":159795937280,"used":120751640576,"free":39044296704,"percent":75.6}},
     * {"device":"D:\\","mountpoint":"D:\\","fstype":"NTFS","opts":"rw,fixed","usage":{"total":94996787200,"used":69929074688,"free":25067712512,"percent":73.6}},
     * {"device":"E:\\","mountpoint":"E:\\","fstype":"NTFS","opts":"rw,fixed","usage":{"total":644245090304,"used":252939661312,"free":391305428992,"percent":39.3}},
     * {"device":"F:\\","mountpoint":"F:\\","fstype":"NTFS","opts":"rw,fixed","usage":{"total":355956944896,"used":157877895168,"free":198079049728,"percent":44.4}}],
     * "timestamp":"2026-03-25 16:12:19"}
     *
     * @param deviceVo
     * @return
     */
    @Override
    public Map<String, Object> selectDeviceInfoByDevice(DeviceVo deviceVo) {
        if (StringUtils.isNotEmpty(deviceVo.getDeviceCode())) {
            return getDeviceInfoMap(deviceVo.getDataCount(), deviceVo.getDeviceCode());
        } else {
            Device device = deviceMapper.selectById(deviceVo.getId());
            if (device == null) {
                return new HashMap<>();
            }
            return getDeviceInfoMap(deviceVo.getDataCount(), device.getDeviceCode());
        }
    }

    private Map<String, Object> getDeviceInfoMap(Integer dataCount, String deviceCode) {
        Map<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<DeviceInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceInfo::getDeviceCode, deviceCode);
        wrapper.orderByDesc(DeviceInfo::getId);
        wrapper.last("LIMIT " + dataCount);
        List<DeviceInfo> list = deviceInfoMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(list)) {
            return map;
        }

        // 单条记录数据
        JSONObject json = JSONObject.parseObject(list.get(0).getDeviceJson());
        map.put("system", json.getString("system"));
        map.put("systemName", json.getString("node_name"));
        Integer cpuCores = json.getJSONObject("cpu").getInteger("physical_cores");
        map.put("cpuCores", cpuCores);
        map.put("memoryTotal", json.getJSONObject("memory").get("total"));

        List<Map<String, Object>> memoryUsed = new ArrayList<>();
        List<Map<String, Object>> cpuUsage = new ArrayList<>();

        list.forEach(item -> {
            JSONObject jsonObject = JSONObject.parseObject(item.getDeviceJson());
            Map<String, Object> memory = new HashMap<>();
            memory.put("value", jsonObject.getJSONObject("memory").getString("used"));
            memory.put("time", item.getCreateTime());
            memoryUsed.add(memory);

            Map<String, Object> cpu = new HashMap<>();
            cpu.put("per", jsonObject.getJSONObject("cpu").getList("usage_per_core", Double.class));
            cpu.put("total", jsonObject.getJSONObject("cpu").getString("total_usage"));
            cpu.put("time", item.getCreateTime());
            cpuUsage.add(cpu);
        });

        map.put("memoryUsed", memoryUsed);
        map.put("cpuUsage", cpuUsage);
        return map;
    }


}
