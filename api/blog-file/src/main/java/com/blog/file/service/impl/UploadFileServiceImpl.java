package com.blog.file.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileUploadVo;
import com.blog.core.enums.file.FilePathEnum;
import com.blog.core.enums.file.FileTypeEnum;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.DateUtil;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.service.UploadFileService;
import com.blog.file.util.VideoUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;

/**
 * @Author: lxk
 * @date 2022/7/7 15:59
 * @description: 文件上传服务
 */

@Service
public class UploadFileServiceImpl implements UploadFileService {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileServiceImpl.class);

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    @Resource
    private MinioService minioService;

    /**
     * 文件上传服务
     *
     * @param uploadVo
     * @return
     * @throws ServiceException
     */
    @Override
    public FileCategoryData uploadService(FileUploadVo uploadVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String userName = SecurityUtil.getLoginUser().getUsername();
        String filePath = FilePathEnum.getFilePathByCode(uploadVo.getFilePathCode());
        String fileName = uploadVo.getFile().getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            throw new ServiceException("文件名称不能为空");
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String typePath = FileTypeEnum.getTypeEnumByFileType(fileType).getFileTypePath();

        String path;
        if (StringUtils.isNotEmpty(uploadVo.getAppointPath())) {
            path = "/" + userId + uploadVo.getAppointPath();
        } else {
            path = "/" + userId + filePath + typePath;
        }

        Integer categoryId = createFileCategory(path, 1);
        String newFileName = DateUtil.formatDateTimeNoSpaces() + "_" + MyStringUtils.getRandomString(6) + "_" + fileName;

        String fileUrl = minioService.uploadFile(uploadVo.getFile(), path + "/" + newFileName);

        if (StringUtils.isNotEmpty(fileUrl)) {
            FileCategoryData fileCategoryData = new FileCategoryData();
            fileCategoryData.setUserId(userId);
            fileCategoryData.setFileName(fileName);
            fileCategoryData.setFileCategoryId(categoryId);
            fileCategoryData.setFileUrl(fileUrl);
            fileCategoryData.setFileSize((int) uploadVo.getFile().getSize());
            fileCategoryData.setFileStatus(0);
            fileCategoryData.setFileType(fileType);
            if (FileTypeEnum.getTypeEnumByFileType(fileType).getFileType() == 3) {
                // 如果是视频文件，解析视频获取时间长度
                fileCategoryData.setFileJson(VideoUtil.resolveVideo(uploadVo.getFile()));
            }
            fileCategoryData.setCreateBy(userName);
            fileCategoryData.setCreateTime(new Date());
            fileCategoryDataMapper.insert(fileCategoryData);
            return fileCategoryData;
        }
        return null;
    }

    @Override
    public void deleteFile(Integer id) throws ServiceException {
        FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(id);
        FileCategory fileCategory = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());
        String fileName = minioService.getFileName(fileCategoryData.getFileUrl());
        minioService.deleteFile(fileCategory.getDirPath(), fileName);
    }

    @Override
    public void deleteFile(List<Integer> idList) throws ServiceException {
        if (CollectionUtils.isNotEmpty(idList)) {
            for (Integer id : idList) {
                FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(id);
                if (fileCategoryData != null) {
                    FileCategory fileCategory = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());
                    String fileName = minioService.getFileName(fileCategoryData.getFileUrl());
                    minioService.deleteFile(fileCategory.getDirPath(), fileName);
                }
            }
        }

    }

    @Override
    public String authFile(String path, Integer time) throws ServiceException {
        return minioService.authFile(path, time);
    }

    /**
     * 获取文件上传目录id
     *
     * @param path 文件上传路径
     * @return 文件直属目录id
     */
    public Integer createFileCategory(String path, Integer dirType) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String userName = SecurityUtil.getLoginUser().getUsername();
        String[] pathArray = path.split("/");
        StringBuilder dirPath = new StringBuilder();
        Integer resultFileCategoryId = 0;
        for (int i = 1; i < pathArray.length; i++) {
            String str = pathArray[i];
            dirPath.append("/").append(str);
            LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategory::getDirPath, dirPath.toString());

            FileCategory category = fileCategoryMapper.selectOne(wrapper);
            if (category == null) {
                try {
                    category = new FileCategory();
                    category.setDirName(str);
                    category.setDirPath(dirPath.toString());
                    category.setDirType(dirType);
                    category.setParentDir(resultFileCategoryId);
                    category.setUserId(userId);
                    category.setCreateBy(userName);
                    category.setCreateTime(new Date());
                    fileCategoryMapper.insert(category);
                } catch (Exception e) {
                    logger.error("创建目录失败: {}", e.getMessage(), e);
                    category = fileCategoryMapper.selectOne(wrapper);
                }
            }
            resultFileCategoryId = category.getId();
        }
        return resultFileCategoryId;
    }


}