package com.blog.auth.controller;

import com.blog.auth.service.RoleService;
import com.blog.core.domain.auth.vo.RoleVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 角色相关接口
 * @Author lxk
 * @CreateTime 2025-03-20
 */

@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 分页查询角色列表
     *
     * @param roleVo
     * @return
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:role:select')")
    public Result selectRoleByPage(RoleVo roleVo) {
        return ResultFactory.buildSuccessResult(roleService.selectRoleList(roleVo));
    }

    /**
     * 创建新角色
     *
     * @param roleVo
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:role:insert')")
    public Result saveRole(@RequestBody RoleVo roleVo) {
        return ResultFactory.buildSuccessResult(roleService.insertRole(roleVo));
    }

    /**
     * 修改角色信息
     *
     * @param roleVo
     * @return
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:role:update')")
    public Result updateRole(@RequestBody RoleVo roleVo) {
        return ResultFactory.buildSuccessResult(roleService.updateRole(roleVo));
    }

    /**
     * 删除角色
     *
     * @param roleVo
     * @return
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:role:delete')")
    public Result deleteRoleByIds(RoleVo roleVo) {
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 查看角色权限
     *
     * @param roleVo
     * @return
     */
    @GetMapping("/permission/select")
    @PreAuthorize("hasAnyAuthority('sys:role:permission:select')")
    public Result selectRolePermission(RoleVo roleVo) {
        return ResultFactory.buildSuccessResult();

    }

    /**
     * 修改角色权限
     *
     * @param roleVo
     * @return
     */
    @PostMapping("/permission/update")
    @PreAuthorize("hasAnyAuthority('sys:role:permission:update')")
    public Result updateRolePermission(@RequestBody RoleVo roleVo) {

        return ResultFactory.buildSuccessResult();

    }

}
