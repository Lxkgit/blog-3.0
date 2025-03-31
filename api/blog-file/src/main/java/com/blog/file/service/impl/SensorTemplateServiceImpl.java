package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.device.dto.SensorTemplateDTO;
import com.blog.core.domain.file.device.entity.Chip;
import com.blog.core.domain.file.device.entity.Sensor;
import com.blog.core.domain.file.device.entity.SensorTemplate;
import com.blog.core.domain.file.device.vo.SensorTemplateVO;
import com.blog.core.exception.ValidException;
import com.blog.file.mapper.ChipMapper;
import com.blog.file.mapper.SensorMapper;
import com.blog.file.mapper.SensorTemplateMapper;
import com.blog.file.service.SensorTemplateService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description 传感器模板服务实现类
 * @Author lxk
 * @CreateTime 2024-09-29
 */

@Service
public class SensorTemplateServiceImpl implements SensorTemplateService {

    @Resource
    private SensorTemplateMapper sensorTemplateDAO;

    @Resource
    private ChipMapper chipDAO;

    @Resource
    private SensorMapper sensorDAO;

    @Override
    public List<SensorTemplateVO> selectSensorTemplateByChipOrSensorId(Integer userId, SensorTemplateDTO sensorTemplateDTO) throws ValidException {
        LambdaQueryWrapper<SensorTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Set<String> sensorTypeSet;
        if (sensorTemplateDTO.getChipId() != null) {
            Chip chip = chipDAO.selectById(sensorTemplateDTO.getChipId());
            sensorTypeSet = sensorDAO.selectList(new LambdaQueryWrapper<Sensor>().eq(Sensor::getDeviceCode, chip.getDeviceCode())
                    .eq(Sensor::getChipCode, chip.getChipCode())).stream().map(Sensor::getSensorType).collect(Collectors.toSet());
        } else if (sensorTemplateDTO.getSensorId() != null) {
            sensorTypeSet = Stream.of(sensorDAO.selectById(sensorTemplateDTO.getSensorId())).map(Sensor::getSensorType).collect(Collectors.toSet());
        } else {
            throw new ValidException("单片机id与传感器id不能同时为空");
        }

        List<SensorTemplate> sensorTemplateList = sensorTemplateDAO.selectList(lambdaQueryWrapper.eq(SensorTemplate::getUserId, userId).in(SensorTemplate::getSensorType, sensorTypeSet));

        List<SensorTemplateVO> sensorTemplateVOList = new ArrayList<>();
        sensorTemplateList.forEach(item -> {
            SensorTemplateVO sensorTemplateVO = new SensorTemplateVO();
            BeanUtils.copyProperties(item, sensorTemplateVO);
            sensorTemplateVOList.add(sensorTemplateVO);
        });

        return sensorTemplateVOList;
    }

}
