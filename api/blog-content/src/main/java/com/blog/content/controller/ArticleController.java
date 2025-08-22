package com.blog.content.controller;


import com.blog.content.service.ArticleService;
import com.blog.core.domain.content.article.vo.ArticleVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.*;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @Author: lxk
 * @date 2022/6/8 20:34
 * @description: 文章服务接口
 */

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    /**
     * 创建文章
     *
     * @param articleVo
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:article:insert')")
    public Result saveArticle(@RequestBody @Validated(value = {InsertGroup.class}) ArticleVo articleVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleService.saveArticle(articleVo));
    }

    /**
     * 删除文章
     *
     * @param articleVo
     * @return
     * @throws ServiceException
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:article:delete')")
    public Result deleteArticle(@Validated(value = {DeleteGroup.class}) ArticleVo articleVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleService.deleteArticle(articleVo.getArticleIds()));
    }

    /**
     * 修改文章
     *
     * @param articleVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:article:update')")
    public Result updateArticle(@RequestBody @Validated(value = {UpdateGroup.class}) ArticleVo articleVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleService.updateArticle(articleVo));
    }

    /**
     * 分页查询文章
     *
     * @param articleVo
     * @return
     * @throws ServiceException
     */
    @GetMapping("/list")
    public Result selectArticleByPage(@Validated(value = {SelectListGroup.class}) ArticleVo articleVo) throws ServiceException {

        ResultPage<ArticleVo> result = articleService.selectArticleListByPageAndUserId(articleVo);
        return ResultFactory.buildSuccessResult(result);
    }

    /**
     * 指定id查询文章
     *
     * @param articleVo
     * @return
     */
    @GetMapping("/id")
    public Result selectArticleById(@Validated(value = {SelectIdGroup.class}) ArticleVo articleVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(articleService.selectArticleById(articleVo.getId()));
    }

}
