package com.blog.file.service;

import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileUploadVo;
import com.blog.core.exception.ServiceException;

/**
 * @author: lxk
 * @date: 2022/7/7 20:50
 * @description:
 * @modified By:
 */
public interface UploadFileService {

    FileCategoryData uploadService(FileUploadVo uploadVo) throws ServiceException;

    void createDir(String path) throws ServiceException;

    void deleteFileDir(String path, String dirName) throws ServiceException;

    void deleteFile(FileCategoryDataVo fileCategoryData) throws ServiceException;
}
