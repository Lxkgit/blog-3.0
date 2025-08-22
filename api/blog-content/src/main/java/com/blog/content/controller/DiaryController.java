package com.blog.content.controller;


import com.blog.content.service.DiaryService;
import com.blog.core.domain.content.diary.entity.Diary;
import com.blog.core.domain.content.diary.vo.DiaryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.InsertGroup;
import com.blog.core.valication.group.DeleteGroup;
import com.blog.core.valication.group.UpdateGroup;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: lxk
 * @date 2022/6/22 16:05
 * @description: 日记接口
 */

@RestController
@RequestMapping("/diary")
public class DiaryController {

    @Resource
    private DiaryService diaryService;

    /**
     * 新增日记
     *
     * @param diaryVo
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:diary:insert')")
    public Result saveDiary(@RequestBody @Validated(value = {InsertGroup.class}) DiaryVo diaryVo) {
        return ResultFactory.buildSuccessResult(diaryService.saveDiary(diaryVo));
    }

    /**
     * 批量删除日记
     *
     * @param diaryVo
     * @return
     * @throws ServiceException
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:diary:delete')")
    public Result deleteDiaryByDate(@Validated(value = {DeleteGroup.class}) DiaryVo diaryVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(diaryService.deleteDiary(diaryVo.getIds()));
    }

    /**
     * 修改日记
     *
     * @param diaryVo
     * @return
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:diary:update')")
    public Result updateDiary(@RequestBody @Validated(value = {UpdateGroup.class}) DiaryVo diaryVo) {
        return ResultFactory.buildSuccessResult(diaryService.updateDiary(diaryVo));
    }

    /**
     * 分页查询日记
     *
     * @param diaryVo
     * @return
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:diary:list')")
    public Result selectDiaryByDate(DiaryVo diaryVo) {
        return ResultFactory.buildSuccessResult(diaryService.selectDiaryByDate(diaryVo));
    }

    /**
     * 内部日记上传接口
     *
     * @param map
     * @return
     */
    @PostMapping("/save/list")
    Map<String, List<String>> saveDiaryList(@RequestBody Map<String, Diary> map) {
        return diaryService.saveDiaryList(map);
    }
}
