package com.blog.content.controller;

import com.blog.content.service.ArticleLabelService;
import com.blog.core.domain.content.article.vo.ArticleLabelVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.AddGroup;
import com.blog.core.valication.group.DeleteGroup;
import com.blog.core.valication.group.SelectListGroup;
import com.blog.core.valication.group.UpdateGroup;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @author: lxk
 * @date: 2022/6/15 18:54
 * @description: 文章标签接口
 * @modified By:
 */

@RestController
@RequestMapping("/article/label")
public class ArticleLabelController {


    @Resource
    private ArticleLabelService articleLabelService;

    /**
     * 创建文章标签
     *
     * @param articleLabelVo
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sys:article:label:insert')")
    public Result saveArticleLabel(@RequestBody @Validated(value = {AddGroup.class}) ArticleLabelVo articleLabelVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleLabelService.saveArticleLabel(articleLabelVo));
    }

    /**
     * 删除文章标签
     *
     * @param articleLabelVo
     * @return
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:article:label:delete')")
    public Result deleteArticleLabelByIds(@Validated(value = {DeleteGroup.class}) ArticleLabelVo articleLabelVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleLabelService.deleteArticleLabelByIds(articleLabelVo.getIds(), 1));
    }

    /**
     * 修改文章标签
     *
     * @param articleLabelVo
     * @return
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:article:label:update')")
    public Result updateArticleLabel(@RequestBody @Validated(value = {UpdateGroup.class}) ArticleLabelVo articleLabelVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleLabelService.updateArticleLabel(articleLabelVo));
    }

    /**
     * 查询文章标签列表
     *
     * @param articleLabelVo
     * @return
     */
    @GetMapping("/list")
    public Result selectArticleLabelList(@Validated(value = {SelectListGroup.class}) ArticleLabelVo articleLabelVo) {
        return ResultFactory.buildSuccessResult(articleLabelService.selectArticleLabelList(articleLabelVo.getLabelType()));
    }
}
