package com.blog.file.controller;

import com.blog.core.domain.file.files.vo.FileDataVo;
import com.blog.core.exception.ValidException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 文件服务接口类
 * @Author: lxk
 * @date 2023/8/2 14:59
 */

@Slf4j
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
     * @throws ValidException
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('sys:file:user:save')")
    public Result saveFileDir(@Validated @RequestBody FileDataVo fileDataVo) throws ValidException {
        fileService.saveFileDir(fileDataVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 删除云盘文件或目录
     *
     * @param fileDataVo
     * @return
     * @throws ValidException
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('sys:file:user:delete')")
    public Result deleteFileOrDir(@Validated FileDataVo fileDataVo) throws ValidException {
        fileService.deleteFileOrDir(fileDataVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 修改云盘文件或目录名称
     *
     * @param fileDataVo
     * @return
     * @throws ValidException
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('sys:file:user:update')")
    public Result updateFileOrDirName(@Validated @RequestBody FileDataVo fileDataVo) throws ValidException {
        fileService.updateFileOrDirName(fileDataVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 查看文件列表
     *
     * @param fileDataVo
     * @return
     */
    @GetMapping("/select")
    @PreAuthorize("hasAnyAuthority('sys:file:user:select')")
    public Result selectFileDir(@Validated FileDataVo fileDataVo) {
        return ResultFactory.buildSuccessResult(fileService.selectFileDir(fileDataVo));
    }

    /**
     * 获取云盘剩余空间大小
     *
     * @return
     */
    @GetMapping("/space")
    @PreAuthorize("hasAnyAuthority('sys:file:user:space')")
    public Result selectUserSpace() {
        return ResultFactory.buildSuccessResult(fileService.selectUserSpace());
    }

    /**
     * 同步文件(包括文件同步至远程和从远程下载文件)
     *
     * @param fileDataVo
     * @return
     */
    @GetMapping("/sync")
    @PreAuthorize("hasAnyAuthority('sys:file:user:sync')")
    public Result syncFile(@Validated FileDataVo fileDataVo) {
        return ResultFactory.buildSuccessResult(fileService.syncFile(fileDataVo));
    }

}
