package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.constant.ErrorMessage;
import com.blog.core.domain.file.device.entity.Chip;
import com.blog.core.domain.file.device.entity.Sensor;
import com.blog.core.domain.file.device.entity.SensorType;
import com.blog.core.domain.file.device.vo.SensorVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.MyPage;
import com.blog.core.result.MyPageUtils;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.ChipMapper;
import com.blog.file.mapper.SensorMapper;
import com.blog.file.mapper.SensorTypeMapper;
import com.blog.file.service.SensorService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 传感器服务业务层
 * @Author: lxk
 * @date 2024/1/30 20:08
 */

@Slf4j
@Service
public class SensorServiceImpl implements SensorService {

    @Resource
    private ChipMapper chipMapper;

    @Resource
    private SensorMapper sensorMapper;

    @Resource
    private SensorTypeMapper sensorTypeMapper;

    /**
     * 创建传感器
     *
     * @param sensorVo
     * @return
     */
    @Override
    public Integer addSensor(SensorVo sensorVo) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        sensorVo.setUserId(userId);
        sensorVo.setSensorStatus(Constant.DEVICE_OFFLINE);
        sensorVo.setCreateTime(new Date());
        sensorVo.setUpdateTime(new Date());
        sensorMapper.insert(sensorVo);
        return sensorVo.getId();
    }

    /**
     * 删除传感器
     *
     * @param ids
     * @return
     */
    @Override
    public Integer deleteSensors(String ids) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        Set<String> idSet = MyStringUtils.splitString(ids, ",");
        sensorMapper.updateSensorStatusByIds(idSet, userId, Constant.DEVICE_DELETE);
        return null;
    }

    /**
     * 修改传感器信息
     *
     * @param sensorVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer updateSensor(SensorVo sensorVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        Sensor sensor = sensorMapper.selectById(sensorVo.getId());
        if (sensor == null) {
            throw new ServiceException(ErrorMessage.SENSOR_NOT_EXISTS);
        }
        if (!sensor.getUserId().equals(userId)) {
            throw new ServiceException(ErrorMessage.SENSOR_USER_ERROR);
        }
        sensorVo.setUserId(userId);
        sensorVo.setUpdateTime(new Date());
        sensorMapper.updateById(sensorVo);
        return sensorVo.getId();
    }

    /**
     * 分页查询传感器
     *
     * @param sensorVoParam
     * @return
     */
    @Override
    public MyPage<SensorVo> selectSensorList(SensorVo sensorVoParam) {
        Integer userId = SecurityUtil.getLoginUser().getId();

        List<SensorType> sensorTypeList = sensorTypeMapper.selectList(null);

        Map<String, SensorType> map = sensorTypeList.stream().collect(Collectors.toMap(SensorType::getSensorType, Function.identity()));

        Chip chip = chipMapper.selectById(sensorVoParam.getChipId());

        LambdaQueryWrapper<Sensor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sensor::getUserId, userId);
        wrapper.eq(Sensor::getDeviceCode, chip.getDeviceCode());
        wrapper.eq(Sensor::getChipCode, chip.getChipCode());

        if (sensorVoParam.getSensorControlType() != null) {
            // 快速获取对应控制类型的传感器类型编码
            wrapper.in(Sensor::getSensorType, sensorTypeMapper.selectList(new LambdaQueryWrapper<SensorType>()
                    .eq(SensorType::getSensorControlType, sensorVoParam.getSensorControlType()))
                    .stream().map(SensorType::getSensorType).collect(Collectors.toList()));
        }

        PageHelper.startPage(sensorVoParam.getPageNum(), sensorVoParam.getPageSize());
        Page<Sensor> sensorPage = (Page<Sensor>) sensorMapper.selectList(wrapper);

        List<SensorVo> sensorVoList = new ArrayList<>();
        for (Sensor sensor : sensorPage) {
            SensorVo sensorVo = new SensorVo();
            BeanUtils.copyProperties(sensor, sensorVo);

            sensorVo.setSensorTypeObj(map.get(sensor.getSensorType()));
            sensorVoList.add(sensorVo);
        }

        return MyPageUtils.pageUtil(sensorVoList, sensorPage.getPageNum(), sensorPage.getPageSize(), (int) sensorPage.getTotal());
    }

    /**
     * 根据id查询传感器信息
     *
     * @param id
     * @return
     */
    @Override
    public SensorVo selectSensorId(Integer id) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        QueryWrapper<Sensor> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        wrapper.eq("user_id", userId);
        Sensor sensor = sensorMapper.selectOne(wrapper);
        SensorVo sensorVo = new SensorVo();
        BeanUtils.copyProperties(sensor, sensorVo);
        return sensorVo;
    }

}
