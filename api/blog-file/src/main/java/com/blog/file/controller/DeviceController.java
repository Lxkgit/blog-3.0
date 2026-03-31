package com.blog.file.controller;

import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.InsertGroup;
import com.blog.core.valication.group.DeleteGroup;
import com.blog.core.valication.group.SelectIdGroup;
import com.blog.core.valication.group.UpdateGroup;
import com.blog.file.service.DeviceService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 下级设备接口
 * @Author: lxk
 * @date 2024/1/29 13:48
 */

@RestController
@RequestMapping("/device")
public class DeviceController {

    @Resource
    private DeviceService deviceService;


    /**
     * 创建远程服务器
     *
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:device:save')")
    public Result addDevice(@Validated(value = {InsertGroup.class}) @RequestBody DeviceVo deviceVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(deviceService.addDevice(deviceVo));
    }

    /**
     * 删除远程服务器
     *
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:device:delete')")
    public Result deleteDevice(@Validated(value = {DeleteGroup.class}) DeviceVo deviceVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(deviceService.deleteDevice(deviceVo.getIds()));
    }

    /**
     * 修改远程服务器配置信息
     *
     * @param deviceVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:device:update')")
    public Result updateDevice(@Validated(value = {UpdateGroup.class}) @RequestBody DeviceVo deviceVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(deviceService.updateDevice(deviceVo));
    }

    /**
     * 查询用户全部的远程服务器设备
     *
     * @return
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:device:select')")
    public Result selectDeviceList() throws ServiceException {
        return ResultFactory.buildSuccessResult(deviceService.selectDeviceList());
    }

    /**
     * 根据id查询服务器设备信息
     *
     * @param deviceVo
     * @return
     */
    @GetMapping("/id")
    @PreAuthorize("hasAnyAuthority('sys:device:select')")
    public Result selectDeviceById(@Validated(value = {SelectIdGroup.class}) DeviceVo deviceVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(deviceService.selectDeviceById(deviceVo.getId()));
    }

    @GetMapping("/status")
    public Result selectDeviceStatus() {
        deviceService.getDeviceStatus();
        return ResultFactory.buildSuccessResult();
    }

    @GetMapping("/info")
    @PreAuthorize("hasAnyAuthority('sys:device:select')")
    public Result selectDeviceInfoByDeviceCode(DeviceVo deviceVo) {
        return ResultFactory.buildSuccessResult(deviceService.selectDeviceInfoByDeviceCode(deviceVo.getDeviceCode(), deviceVo.getDataCount()));
    }



}
