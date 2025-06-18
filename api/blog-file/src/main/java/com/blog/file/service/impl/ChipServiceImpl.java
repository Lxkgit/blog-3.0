package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.constant.ErrorMessage;
import com.blog.core.domain.file.device.entity.Chip;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.entity.Sensor;
import com.blog.core.domain.file.device.vo.ChipVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.MyPage;
import com.blog.core.result.MyPageUtils;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.ChipMapper;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.mapper.SensorMapper;
import com.blog.file.service.ChipService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description: 单片机服务层方法
 * @Author: lxk
 * @date 2024/1/30 19:58
 */

@Service
public class ChipServiceImpl implements ChipService {

    @Resource
    private ChipMapper chipMapper;

    @Resource
    private SensorMapper sensorMapper;

    @Resource
    private DeviceMapper deviceMapper;

    /**
     * 新增单片机
     *
     * @param chipVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer addChip(ChipVo chipVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        QueryWrapper<Chip> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("chip_code", chipVo.getChipCode());
        Chip chip = chipMapper.selectOne(wrapper);
        if (chip != null) {
            throw new ServiceException(ErrorMessage.CHIP_CODE_EXISTS);
        }
        chipVo.setUserId(userId);
        chipVo.setChipStatus(Constant.DEVICE_OFFLINE);
        chipVo.setCreateTime(new Date());
        chipVo.setUpdateTime(new Date());
        chipMapper.insert(chipVo);
        return chipVo.getId();
    }

    /**
     * 批量删除单片机
     *
     * @param ids
     * @return
     */
    @Override
    public Integer deleteChips(String ids) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        Set<String> idSet = MyStringUtils.splitString(ids, ",");
        chipMapper.updateChipStatus(idSet, userId, Constant.DEVICE_DELETE);
        return idSet.size();
    }

    /**
     * 修改单片机信息
     *
     * @param chipVo
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer updateChip(ChipVo chipVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        QueryWrapper<Chip> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("chip_code", chipVo.getChipCode());
        wrapper.ne("id", chipVo.getId());
        Chip chip = chipMapper.selectOne(wrapper);
        if (chip != null) {
            throw new ServiceException(ErrorMessage.CHIP_CODE_EXISTS);
        }
        chipVo.setUserId(userId);
        chipVo.setChipStatus(Constant.DEVICE_OFFLINE);
        chipVo.setUpdateTime(new Date());
        chipMapper.updateById(chipVo);
        return chipVo.getId();
    }

    /**
     * 分页查询单片机
     *
     * @param chipVoParam
     * @return
     */
    @Override
    public MyPage<ChipVo> selectChipList(ChipVo chipVoParam) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        Device device = deviceMapper.selectById(chipVoParam.getDeviceId());

        LambdaQueryWrapper<Chip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Chip::getUserId, userId);
        wrapper.eq(Chip::getDeviceCode, device.getDeviceCode());
        wrapper.ne(Chip::getChipStatus, Constant.DEVICE_DELETE);

        PageHelper.startPage(chipVoParam.getPageNum(), chipVoParam.getPageSize());
        Page<Chip> chipPage = (Page<Chip>) chipMapper.selectList(wrapper);

        List<ChipVo> chipVoList = new ArrayList<>();
        for (Chip chip : chipPage) {
            ChipVo chipVo = new ChipVo();
            BeanUtils.copyProperties(chip, chipVo);
            chipVoList.add(chipVo);
        }

        return MyPageUtils.pageUtil(chipVoList, chipPage.getPageNum(), chipPage.getPageSize(), (int) chipPage.getTotal());
    }

    /**
     * 查询指定单片机信息
     *
     * @param id
     * @return
     */
    @Override
    public ChipVo selectChipId(Integer id) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        LambdaQueryWrapper<Chip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Chip::getId, id);
        wrapper.eq(Chip::getUserId, userId);
        Chip chip = chipMapper.selectOne(wrapper);

        ChipVo chipVo = new ChipVo();
        BeanUtils.copyProperties(chip, chipVo);

        LambdaQueryWrapper<Sensor> sensorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sensorLambdaQueryWrapper.eq(Sensor::getDeviceCode, chip.getDeviceCode());
        sensorLambdaQueryWrapper.eq(Sensor::getChipCode, chip.getChipCode());
        List<Sensor> sensorList = sensorMapper.selectList(sensorLambdaQueryWrapper);

        chipVo.setSensorList(sensorList);
        return chipVo;
    }

    @Override
    public ChipVo selectChipInfo(ChipVo chipVo) {
        

        return null;
    }
}
