package com.blog.content.controller;

import com.blog.content.service.DocService;
import com.blog.core.domain.content.doc.entity.DocCatalog;
import com.blog.core.domain.content.doc.entity.DocContent;
import com.blog.core.domain.content.doc.vo.DocCatalogVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lxk
 * @date 2022/6/20 9:24
 * @description:
 */

@Slf4j
@RestController
@RequestMapping("/doc")
public class DocController {

    @Resource
    private DocService docService;

    @PutMapping("/content/insert")
    @PreAuthorize("hasAnyAuthority('sys:doc:insert')")
    public Result insertDocCatalog(@RequestBody DocCatalog docCatalog) {


        return ResultFactory.buildSuccessResult(docService.insertDocCatalog(docCatalog));
    }

    @DeleteMapping("/content/delete")
    @PreAuthorize("hasAnyAuthority('sys:doc:delete')")
    public Result deleteDocContent(@RequestParam(value = "id") Integer id) {
        return ResultFactory.buildSuccessResult(docService.deleteDocCatalog(id));
    }

    @PostMapping("/content/update")
    @PreAuthorize("hasAnyAuthority('sys:doc:update')")
    public Result updateDocContent(@RequestBody DocContent docContent) {
        return ResultFactory.buildSuccessResult(docService.updateDocContent(docContent));
    }

    @PostMapping("/catalog/update")
    @PreAuthorize("hasAnyAuthority('sys:doc:catalog:update')")
    public Result updateDocCatalog(@RequestBody DocCatalog docCatalog) {
        return ResultFactory.buildSuccessResult(docService.updateDocCatalog(docCatalog));
    }

    @GetMapping("/catalog/tree")
    public Result selectDocCatalogTree(DocCatalogVo docCatalogVo) {

        return ResultFactory.buildSuccessResult(docService.selectDocCatalogTree(docCatalogVo));
    }

    @GetMapping("/content/id")
    public Result selectDocContentById(@RequestParam(value = "id") Integer id) {
        return ResultFactory.buildSuccessResult(docService.selectDocContentById(id));
    }

    @GetMapping("/catalog/id")
    public Result selectDocCatalogById(@RequestParam(value = "id") Integer id) {
        return ResultFactory.buildSuccessResult(docService.selectDocCatalogById(id));
    }

    @GetMapping("/content/user")
    public Result selectDocUserList() {
        return ResultFactory.buildSuccessResult(docService.selectDocUserList());
    }

}