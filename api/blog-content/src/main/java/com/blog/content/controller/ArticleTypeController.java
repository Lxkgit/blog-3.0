package com.blog.content.controller;


import com.blog.content.service.ArticleTypeService;
import com.blog.core.domain.content.article.vo.ArticleTypeVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @Author: lxk
 * @date 2022/6/14 16:30
 * @description: 文章类型控制类
 */

@RestController
@RequestMapping("/article/type")
public class ArticleTypeController {

    @Resource
    private ArticleTypeService articleTypeService;

    /**
     * 创建文章分类
     *
     * @param articleTypeVo
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:article:type:insert')")
    public Result saveArticleType(@RequestBody @Validated(value = {AddGroup.class}) ArticleTypeVo articleTypeVo) {
        return ResultFactory.buildSuccessResult(articleTypeService.saveArticleType(articleTypeVo));
    }

    /**
     * 删除文章分类
     *
     * @param articleTypeVo
     * @return
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:article:type:delete')")
    public Result deleteArticleType(@Validated(value = {DeleteGroup.class}) ArticleTypeVo articleTypeVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleTypeService.deleteArticleTypeById(articleTypeVo.getArticleTypeId()));
    }

    /**
     * 修改文章分类
     *
     * @param articleTypeVo
     * @return
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:article:type:update')")
    public Result updateArticleType(@RequestBody @Validated(value = {UpdateGroup.class}) ArticleTypeVo articleTypeVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleTypeService.updateArticleType(articleTypeVo));
    }

    /**
     * 查询文章分类列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result selectArticleTypeList() {
        return ResultFactory.buildSuccessResult(articleTypeService.selectArticleTypeList());
    }

    /**
     * 根据传入的父节点id获取子节点
     *
     * @param articleTypeVo
     * @return
     */
    @GetMapping("/node")
    public Result selectArticleTypeListByParentId(@Validated(value = {SelectListGroup.class}) ArticleTypeVo articleTypeVo) {
        return ResultFactory.buildSuccessResult(articleTypeService.selectArticleTypeByParentId(articleTypeVo.getParentId()));
    }

    /**
     * 查询指定分类，一直到基础分类
     *
     * @param articleTypeVo
     * @return
     */
    @GetMapping("/id")
    public Result selectArticleTypeById(@Validated(value = {SelectIdGroup.class}) ArticleTypeVo articleTypeVo) {
        return ResultFactory.buildSuccessResult(articleTypeService.selectArticleTypeById(articleTypeVo.getId()));
    }

    /**
     * 查询全部分类，并按照树组织结构返回
     *
     * @return
     */
    @GetMapping("/tree")
    public Result selectArticleTypeTree() {
        return ResultFactory.buildSuccessResult(articleTypeService.selectArticleTypeTree());
    }
}
