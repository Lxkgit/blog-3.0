package com.blog.auth.controller;


import com.blog.auth.service.MenuService;
import com.blog.core.domain.auth.vo.MenuVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: lxk
 * @date 2022/6/24 17:24
 * @description: 系统菜单接口
 */

@Slf4j
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    /**
     * 获取用户权限菜单
     * type = 1 获取到目录
     * type = 2 获取到操作
     * @param menuVo 权限区分
     * @return
     */
    @GetMapping("/list/user")
    public Result getSysMenuList(MenuVo menuVo){
        List<MenuVo> list = menuService.selectMenuListByUser(menuVo);
        return ResultFactory.buildSuccessResult(list);
    }

    /**
     * 获取全部菜单
     * type = 1 获取到目录
     * type = 2 获取到操作
     * @param menuVo
     * @return
     */
    @GetMapping("/list/all")
    public Result getSysMenuAllList(MenuVo menuVo){
        List<MenuVo> list = menuService.selectAllMenu(menuVo);
        return ResultFactory.buildSuccessResult(list);
    }
}
