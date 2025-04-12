package com.blog.file.service.impl;


import com.blog.core.domain.file.files.entity.UploadFile;
import com.blog.core.domain.file.files.entity.UploadLog;
import com.blog.core.domain.file.files.vo.UploadVo;
import com.blog.core.enums.file.FilePathEnum;
import com.blog.core.enums.file.FileTypeEnum;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.utils.DateUtil;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.UploadFileMapper;
import com.blog.file.mapper.UploadLogMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.service.UploadFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: lxk
 * @date 2022/7/7 15:59
 * @description: 文件上传服务
 */

@Slf4j
@Service
public class UploadFileServiceImpl implements UploadFileService {

    @Resource
    private UploadLogMapper uploadLogMapper;

    @Resource
    private UploadFileMapper uploadFileMapper;

    @Resource
    private MinioService minioService;

    @Override
    public String uploadService(UploadVo uploadVo) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String filePath = FilePathEnum.getFilePathByCode(uploadVo.getFilePathCode());
        String fileName = uploadVo.getFiles().getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String typePath = FileTypeEnum.getTypeListByTypeName(fileType);

        String path = "/" + userId + "/" + filePath + "/" + typePath + "/" + DateUtil.formatDateTime(new Date()) + "_" + MyStringUtils.getRandomString(6) + "_" + fileName;
        minioService.uploadFile(uploadVo.getFiles(), path);

        return path;
    }

    //    @Override
//    public Result upload(MultipartFile[] files, Integer userId, String filePath) {
//        Date date = new Date();
//        List<String> result = new ArrayList<>();
//        for (MultipartFile file : files) {
//            String fileName = file.getOriginalFilename();
//            if (fileName != null && !fileName.equals("")) {
//                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
//                String formatDate = DateUtil.formatDateTime(date).replace(" ", "_").replace(":", "-");
//                String newFileName = formatDate + "_" + MyStringUtils.getRandomString(6) + "_" + fileName;
//                UploadLog uploadLog = new UploadLog(userId, newFileName, fileType, 0, "", date);
//                uploadLogMapper.insert(uploadLog);
//                try {
//                    File targetFile;
//                    File file1 = new File(basePath + filePath);
//                    if (!file1.exists() && !file1.isDirectory()) {
//                        file1.mkdirs();
//                    }
//                    targetFile = new File(file1, newFileName);
//                    file.transferTo(targetFile);
//                    String url = serviceIp + baseUri + filePath + "/" + newFileName;
//
//                    result.add(url);
//                    uploadFileMapper.insert(new UploadFile(userId, newFileName, url, date, fileType, basePath + filePath));
//                    uploadLogMapper.updateById(new UploadLog(uploadLog.getId(), userId, 1, "文件上传成功"));
//
//                } catch (Exception e) {
//                    uploadLogMapper.updateById(new UploadLog(uploadLog.getId(), userId, 2, "文件上传失败"));
//                    log.error(e.getMessage(), e);
//                }
//            }
//        }
//        return ResultFactory.buildSuccessResult(result);
//    }

}