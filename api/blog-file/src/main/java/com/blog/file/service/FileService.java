package com.blog.file.service;

import com.blog.core.domain.file.files.entity.FileCategory;
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

    List<FileCategoryDataVo> selectFile(FileCategoryVo fileCategoryVo) throws Exception;

    void createDir(FileCategoryVo fileDataVo) throws ServiceException;

    void deleteFileDir(FileCategoryVo fileCategoryVo) throws ServiceException;

    void moveFile(FileCategoryVo fileCategoryVo) throws ServiceException;

    void deleteFile(List<Integer> idList) throws ServiceException;

    void syncFile(FileCategoryDataVo fileDataVo) throws ServiceException;

//    void fileImportMinio(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead);

//    void fileDownloadDevice(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead);
//
//    public void fileUploadService(NettySyncFileDto nettySyncFileDto);
//
//    boolean syncFileList(List<FileCategoryDataVo> fileDataVoList);
}
