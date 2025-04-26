package com.blog.auth.controller;

import com.blog.auth.config.oauth.service.AuthService;
import com.blog.auth.service.UserService;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.AddGroup;
import com.blog.core.valication.group.SelectListGroup;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 用户接口
 * @Author lxk
 * @CreateTime 2025-03-24
 */

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping(value = "/select/id")
    public UserVo selectUserById(@RequestParam(value = "userId") Integer userId){
        return userService.selectUserById(userId);
    }

    @GetMapping("/id")
    public Result getUserById(@RequestParam(value = "id") Integer id) {
        return ResultFactory.buildSuccessResult(userService.selectUserById(id));
    }

    @GetMapping("/username")
    public Result getUserByUsername(@RequestParam(value = "username") String username) {
        return ResultFactory.buildSuccessResult(userService.selectUserByUsername(username));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:user:list')")
    public Result selectUserList(@Validated(value = {SelectListGroup.class}) UserVo userVo) {
        return ResultFactory.buildSuccessResult(userService.selectUserByPage(userVo));
    }

    /**
     * 用户修改个人信息
     * @return
     */
    @PostMapping("/update")
    public Result updateUserMsg(@RequestBody UserVo userVo) {
        userService.updateUser(userVo, 0);
        return ResultFactory.buildSuccessResult();
    }

    @GetMapping(value = "/select/user/id")
    public Result selectUserMsgById(@RequestParam(value = "userId") Integer userId){
        return ResultFactory.buildSuccessResult(userService.selectUserById(userId));
    }

    @PostMapping("/permission/update")
    public Result updateUserPermission(@RequestBody UserVo userVo) {
        userService.updateUserPermission(userVo);
        return ResultFactory.buildSuccessResult();
    }

}
