package com.blog.file.controller;

import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.UserLocationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 锚点接口服务
 * @Author lxk
 * @CreateTime 2026-09-24
 */

@RestController
@RequestMapping("/point")
public class AnchorPointController {


    @Resource
    private UserLocationService userLocationService;

    /**
     * 查询服务器ip地址定位列表
     *
     * @return
     */
    @GetMapping("/ip")
    public Result selectIpLocationList() {
        return ResultFactory.buildSuccessResult(userLocationService.selectIpLocationList());
    }

}
