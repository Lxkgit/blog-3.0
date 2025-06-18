package com.blog.file.controller;

import com.blog.core.domain.file.system.vo.BlogSettingVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.BlogSettingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @Author: lxk
 * @date 2023/7/25 17:21
 */

@RestController
@RequestMapping("/setting")
public class BlogSettingController {

    @Resource
    private BlogSettingService blogSettingService;

    @GetMapping("/id")
    public Result selectSettingById(@RequestParam(value = "id") Integer id) {
        return ResultFactory.buildSuccessResult(blogSettingService.selectBlogSettingById(id));
    }

    @GetMapping("/select")
    public Result selectSettingList(@RequestParam(value = "settingType") String settingType) {
        return ResultFactory.buildSuccessResult(blogSettingService.selectBlogSetting(settingType));
    }

    @PostMapping("/update")
    public Result updateSetting(@RequestBody BlogSettingVo blogSettingVo) {
        blogSettingService.updateBlogSetting(blogSettingVo);
        return ResultFactory.buildSuccessResult();
    }
}
