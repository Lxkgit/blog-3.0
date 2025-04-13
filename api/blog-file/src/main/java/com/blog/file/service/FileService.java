package com.blog.file.service;

import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;

import java.util.List;

/**
 * @description: 文件服务类
 * @Author: lxk
 * @date 2023/8/2 15:00
 */

public interface FileService {

    List<FileCategory> selectFileDir(FileCategoryVo fileCategoryVo);

    List<FileCategoryData> selectFile(FileCategoryVo fileCategoryVo);

    void saveFileDir(FileCategoryVo fileDataVo) throws ServiceException;

    void deleteFileDir(FileCategoryVo fileCategoryVo) throws ServiceException;
//
//    void updateFileOrDirName(FileCategoryDataVo fileDataVo) throws ServiceException;
//
//    Long selectUserSpace();
//
//    boolean syncFile(FileCategoryDataVo fileDataVo);
//
//    boolean syncFileList(List<FileCategoryDataVo> fileDataVoList);
}
