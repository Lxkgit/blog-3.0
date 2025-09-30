package com.blog.file.controller;

import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.FileService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 文件服务接口类
 * @Author: lxk
 * @date 2023/8/2 14:59
 */

@RestController
@RequestMapping("/dir")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 创建云盘目录
     *
     * @param fileDataVo
     * @return
     * @throws ServiceException
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:file:user:save')")
    public Result createDir(@Validated @RequestBody FileCategoryVo fileDataVo) throws ServiceException {
        fileService.createDir(fileDataVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 删除云盘目录
     *
     * @param fileCategoryVo
     * @return
     * @throws ServiceException
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:file:user:delete')")
    public Result deleteFileDir(@Validated FileCategoryVo fileCategoryVo) throws ServiceException {
        fileService.deleteFileDir(fileCategoryVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     *
     * @param idList
     * @return
     * @throws ServiceException
     */
    @DeleteMapping("/delete/file")
    @PreAuthorize("hasAnyAuthority('sys:file:user:delete')")
    public Result deleteFile(@Validated @RequestParam(value = "idList") List<Integer> idList) throws ServiceException {
        fileService.deleteFile(idList);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 查看文件列表
     *
     * @param fileCategoryVo
     * @return
     */
    @GetMapping("/select")
    @PreAuthorize("hasAnyAuthority('sys:file:user:select')")
    public Result selectFileDir(@Validated FileCategoryVo fileCategoryVo) {
        return ResultFactory.buildSuccessResult(fileService.selectFileDir(fileCategoryVo));
    }

    /**
     * 查看文件列表
     *
     * @param fileCategoryVo
     * @return
     */
    @GetMapping("/select/file")
    @PreAuthorize("hasAnyAuthority('sys:file:user:select')")
    public Result selectFile(@Validated FileCategoryVo fileCategoryVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(fileService.selectFile(fileCategoryVo));
    }

    /**
     * 查看文件列表
     *
     * @param fileCategoryVo
     * @return
     */
    @PostMapping("/move/file")
    public Result moveFile(@RequestBody FileCategoryVo fileCategoryVo) throws ServiceException {
        fileService.moveFile(fileCategoryVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 同步文件(包括文件同步至远程和从远程下载文件)
     *
     * @param fileDataVo
     * @return
     */
    @GetMapping("/sync/file")
    @PreAuthorize("hasAnyAuthority('sys:file:user:sync')")
    public Result syncFile(@Validated FileCategoryDataVo fileDataVo) throws ServiceException {
        fileService.syncFile(fileDataVo);
        return ResultFactory.buildSuccessResult();
    }




}
