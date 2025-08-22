package com.blog.file.controller;

import com.blog.core.domain.file.device.vo.SensorControlVo;
import com.blog.core.domain.file.device.vo.SensorDataVo;
import com.blog.core.domain.file.device.vo.SensorVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.*;
import com.blog.file.service.SensorControlService;
import com.blog.file.service.SensorDataService;
import com.blog.file.service.SensorService;
import com.blog.file.service.SensorTypeService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 传感器接口类
 * @Author: lxk
 * @date 2024/1/30 20:07
 */

@RestController
@RequestMapping("/sensor")
public class SensorController {

    @Resource
    private SensorService sensorService;

    @Resource
    private SensorControlService sensorControlService;

    @Resource
    private SensorDataService sensorDataService;

    @Resource
    private SensorTypeService sensorTypeService;

    /**
     * 创建传感器
     *
     * @param sensorVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:sensor:save')")
    public Result addSensor( @Validated(value = {InsertGroup.class}) @RequestBody SensorVo sensorVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(sensorService.addSensor(sensorVo));
    }

    /**
     * 删除传感器
     *
     * @param sensorVo
     * @return
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:sensor:delete')")
    public Result deleteSensor( @Validated(value = {DeleteMapping.class}) SensorVo sensorVo) {
        return ResultFactory.buildSuccessResult(sensorService.deleteSensors(sensorVo.getIds()));
    }

    /**
     * 修改传感器数据
     *
     * @param sensorVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:sensor:update')")
    public Result updateSensor( @Validated(value = {UpdateGroup.class}) @RequestBody SensorVo sensorVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(sensorService.updateSensor(sensorVo));
    }

    /**
     * 获取单片机下全部传感器
     *
     * @param sensorVo
     * @return
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:sensor:select')")
    public Result selectSensorList( @Validated(value = {SelectListGroup.class}) SensorVo sensorVo) {
        return ResultFactory.buildSuccessResult(sensorService.selectSensorList(sensorVo));
    }

    /**
     * 根据id获取指定传感器下数据
     * 数据类传感器返回传感器上报的数据信息
     * 控制类传感器返回已创建的控制指令发送命令时间以及当前传感器状态
     *
     * @param sensorVo
     * @return
     */
    @GetMapping("/id")
    @PreAuthorize("hasAnyAuthority('sys:sensor:select')")
    public Result selectSensorId( @Validated(value = {SelectIdGroup.class}) SensorVo sensorVo) {
        return ResultFactory.buildSuccessResult(sensorService.selectSensorId(sensorVo.getId()));
    }

    /**
     * 查询平台支持的全部传感器，权限与新增传感器一致
     *
     * @return
     */
    @GetMapping("/type/list")
    @PreAuthorize("hasAnyAuthority('sys:sensor:save')")
    public Result selectSensorTypeList() {
        return ResultFactory.buildSuccessResult(sensorTypeService.selectSensorTypeList());
    }

    /**
     * 保存传感器控制指令
     *
     * @param sensorControlVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/control/save")
    @PreAuthorize("hasAnyAuthority('sys:sensor:control:save')")
    public Result addSensorControl( @Validated(value = {InsertGroup.class}) @RequestBody SensorControlVo sensorControlVo) throws ServiceException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        return ResultFactory.buildSuccessResult(sensorControlService.createSensorControl(sensorControlVo));
    }

    /**
     * 删除传感器指令
     *
     * @param sensorControlVo
     * @return
     */
    @DeleteMapping("/control/delete")
    @PreAuthorize("hasAnyAuthority('sys:sensor:control:delete')")
    public Result deleteSensorControl( @Validated(value = {DeleteGroup.class}) SensorControlVo sensorControlVo) {
        return ResultFactory.buildSuccessResult(sensorControlService.deleteSensorControl(sensorControlVo.getIds()));
    }

    /**
     * 修改传感器控制指令
     *
     * @param sensorControlVo
     * @return
     */
    @PostMapping("/control/update")
    @PreAuthorize("hasAnyAuthority('sys:sensor:control:update')")
    public Result updateSensorControl( @Validated(value = {UpdateGroup.class}) @RequestBody SensorControlVo sensorControlVo) {
        return ResultFactory.buildSuccessResult(sensorControlService.updateSensorControl(sensorControlVo));
    }

    /**
     * 分页查询传感器控制指令
     *
     * @param sensorControlVo
     * @return
     */
    @GetMapping("/control/list")
    @PreAuthorize("hasAnyAuthority('sys:sensor:control:select')")
    public Result selectSensorControlList( @Validated(value = {SelectListGroup.class}) SensorControlVo sensorControlVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(sensorControlService.selectSensorControlList(sensorControlVo));
    }

    /**
     * 根据id查询传感器控制指令
     *
     * @param sensorControlVo
     * @return
     */
    @GetMapping("/control/id")
    @PreAuthorize("hasAnyAuthority('sys:sensor:control:select')")
    public Result selectSensorControlById( @Validated(value = {SelectIdGroup.class}) SensorControlVo sensorControlVo) {
        return ResultFactory.buildSuccessResult(sensorControlService.selectSensorControlById(sensorControlVo.getId()));
    }

    /**
     * 发送传感器控制命令
     *
     * @param sensorControlVo
     * @return
     */
    @GetMapping("/control/send")
    @PreAuthorize("hasAnyAuthority('sys:sensor:control:send')")
    public Result controlSensor( @Validated(value = {SelectIdGroup.class}) SensorControlVo sensorControlVo) throws ServiceException {
        Boolean flag = sensorControlService.controlSensor(sensorControlVo.getId());
        if (flag) {
            return ResultFactory.buildSuccessResult();
        }
        return ResultFactory.buildFailResult("命令下发失败");
    }

    /**
     * 查询传感器数据
     *
     * @param sensorDataVo
     * @return
     */
    @GetMapping("/data")
    @PreAuthorize("hasAnyAuthority('sys:sensor:data:select')")
    public Result selectSensorDataList( @Validated(value = {SelectIdGroup.class}) SensorDataVo sensorDataVo) {
        return ResultFactory.buildSuccessResult(sensorDataService.selectSensorDataList(sensorDataVo));
    }

}
