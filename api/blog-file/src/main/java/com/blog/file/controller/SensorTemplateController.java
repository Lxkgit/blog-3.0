package com.blog.file.controller;

import com.blog.core.domain.file.device.dto.SensorTemplateDTO;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.SensorTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 传感器模板接口类
 * @Author lxk
 * @CreateTime 2024-09-29
 */

@Slf4j
@RestController
@RequestMapping("/sensorTemplate")
public class SensorTemplateController {

    @Resource
    private SensorTemplateService sensorTemplateService;

    /**
     *
     * @return
     */
    @GetMapping("/chipOrSensorId")
    public Result selectSensorTemplateByChipOrSensorId(SensorTemplateDTO sensorTemplateDTO) throws ServiceException {
        return ResultFactory.buildSuccessResult(sensorTemplateService.selectSensorTemplateByChipOrSensorId(1, sensorTemplateDTO));
    }

}
