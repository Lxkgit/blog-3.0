package com.blog.file.service;

import com.blog.core.domain.file.files.vo.FileDataVo;
import com.blog.core.exception.ServiceException;

import java.util.List;

/**
 * @description: 文件服务类
 * @Author: lxk
 * @date 2023/8/2 15:00
 */

public interface FileService {

    List<FileDataVo> selectFileDir(FileDataVo fileDataVo);

    void saveFileDir(FileDataVo fileDataVo) throws ServiceException;

    void deleteFileOrDir(FileDataVo fileDataVo) throws ServiceException;

    void updateFileOrDirName(FileDataVo fileDataVo) throws ServiceException;

    Long selectUserSpace();

    boolean syncFile(FileDataVo fileDataVo);

    boolean syncFileList(List<FileDataVo> fileDataVoList);
}
