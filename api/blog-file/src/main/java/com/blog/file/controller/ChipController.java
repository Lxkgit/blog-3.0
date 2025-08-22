package com.blog.file.controller;

import com.blog.core.domain.file.device.vo.ChipVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.InsertGroup;
import com.blog.core.valication.group.SelectIdGroup;
import com.blog.core.valication.group.SelectListGroup;
import com.blog.core.valication.group.UpdateGroup;
import com.blog.file.service.ChipService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 单片机服务接口
 * @Author: lxk
 * @date 2024/1/30 19:56
 */

@RestController
@RequestMapping("/chip")
public class ChipController {

    @Resource
    private ChipService chipService;

    /**
     * 新增单片机接口
     *
     * @param chipVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:chip:save')")
    public Result addChip(@Validated(value = {InsertGroup.class}) @RequestBody ChipVo chipVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(chipService.addChip(chipVo));
    }

    /**
     * 删除单片机
     *
     * @param chipVo
     * @return
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:chip:delete')")
    public Result deleteChip(@Validated(value = {DeleteMapping.class}) ChipVo chipVo) {
        return ResultFactory.buildSuccessResult(chipService.deleteChips(chipVo.getIds()));
    }

    /**
     * 修改单片机信息
     *
     * @param chipVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:chip:update')")
    public Result updateChip(@Validated(value = {UpdateGroup.class}) @RequestBody ChipVo chipVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(chipService.updateChip(chipVo));
    }

    /**
     * 分页查询单片机列表
     *
     * @param chipVo
     * @return
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:chip:select')")
    public Result selectChipList(@Validated(value = {SelectListGroup.class}) ChipVo chipVo) {
        return ResultFactory.buildSuccessResult(chipService.selectChipList(chipVo));
    }

    /**
     * 查询指定单片机信息
     *
     * @param chipVo
     * @return
     */
    @GetMapping("/id")
    @PreAuthorize("hasAnyAuthority('sys:chip:select')")
    public Result selectChipId(@Validated(value = {SelectIdGroup.class}) ChipVo chipVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(chipService.selectChipId(chipVo.getId()));
    }

    @GetMapping("/info")
    @PreAuthorize("hasAnyAuthority('sys:chip:select')")
    public Result selectChipInfo(@Validated(value = {SelectIdGroup.class}) ChipVo chipVo) {
        return ResultFactory.buildSuccessResult(chipService.selectChipInfo(chipVo));
    }
}
