package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.FileMultipartFileConverter;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.dto.file.NettyFileSyncDto;
import com.blog.file.netty.service.NettyFileSyncService;
import com.blog.file.service.FileService;
import com.blog.file.service.UploadFileService;
import com.blog.file.utils.VideoUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * @description: 文件服务
 * @Author: lxk
 * @date 2023/8/2 16:03
 */

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    @Resource
    private UploadFileService uploadFileService;

    @Resource
    private MinioService minioService;

    @Lazy
    @Resource
    private NettyFileSyncService nettyFileSyncService;

    /**
     * 创建云盘目录
     *
     * @param fileCategoryVo
     * @throws ServiceException
     */
    @Override
    public void createDir(FileCategoryVo fileCategoryVo) throws ServiceException {
        if (StringUtils.isEmpty(fileCategoryVo.getDirPath())) {
            createDir("/" + fileCategoryVo.getDirName(), fileCategoryVo.getDirType());
        } else {
            createDir(fileCategoryVo.getDirPath() + "/" + fileCategoryVo.getDirName(), fileCategoryVo.getDirType());
        }
    }

    /**
     * 创建目录
     *
     * @param path 目录
     */
    public void createDir(String path, Integer dirType) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String createDir = "/" + userId + path;
        uploadFileService.createFileCategory(createDir, dirType);
    }

    /**
     * 创建目录
     *
     * @param path 目录
     */
    public Integer createDirWithUserId(String path) {
        SecurityUtil.setSystem();
        return uploadFileService.createFileCategory(path, 1);
    }

    /**
     * 删除云盘中文件目录
     *
     * @param fileCategoryVo
     * @throws ServiceException
     */
    @Override
    public void deleteFileDir(FileCategoryVo fileCategoryVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String allPath = "/" + userId + fileCategoryVo.getDirPath();
        String dirName = fileCategoryVo.getDirName();
        LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileCategory::getUserId, userId);
        wrapper.eq(FileCategory::getDirName, dirName);
        wrapper.eq(FileCategory::getDirPath, allPath + "/" + dirName);
        FileCategory fileCategory = fileCategoryMapper.selectOne(wrapper);
        if (fileCategory == null) {
            throw new ServiceException("目录不存在");
        }
        LambdaQueryWrapper<FileCategory> parentWrapper = new LambdaQueryWrapper<>();
        parentWrapper.eq(FileCategory::getParentDir, fileCategory.getId());
        List<FileCategory> categoryList = fileCategoryMapper.selectList(parentWrapper);
        if (CollectionUtils.isNotEmpty(categoryList)) {
            throw new ServiceException("当前目录下存在未删除目录");
        }
        LambdaQueryWrapper<FileCategoryData> dataWrapper = new LambdaQueryWrapper<>();
        dataWrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
        List<FileCategoryData> fileCategoryDataList = fileCategoryDataMapper.selectList(dataWrapper);
        if (CollectionUtils.isNotEmpty(fileCategoryDataList)) {
            throw new ServiceException("当前目录下存在未删除文件");
        }
        fileCategoryMapper.deleteById(fileCategory.getId());
    }


    /**
     * 删除云盘文件
     *
     * @param idList
     * @throws ServiceException
     */
    @Override
    public void deleteFile(List<Integer> idList) throws ServiceException {
        if (CollectionUtils.isNotEmpty(idList)) {
            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getUserId, SecurityUtil.getLoginUser().getId());
            wrapper.in(FileCategoryData::getId, idList);
            fileCategoryDataMapper.delete(wrapper);
            uploadFileService.deleteFile(idList);
        }
    }

    /**
     * 文件移动
     *
     * @param fileCategoryVo
     * @throws ServiceException
     */
    @Override
    public void moveFile(FileCategoryVo fileCategoryVo) throws ServiceException {
        FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(fileCategoryVo.getId());
        FileCategory oldFileCategory = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());
        FileCategory newFileCategory = fileCategoryMapper.selectById(fileCategoryVo.getNewDirId());
        String fileUrl = minioService.moveFile(oldFileCategory.getDirPath() + "/" + fileCategoryData.getFileUrl().substring(fileCategoryData.getFileUrl().lastIndexOf("/") + 1),
                oldFileCategory.getDirPath() + "/" + newFileCategory.getDirName()+ "/");

        // 更新文件信息
        fileCategoryData.setFileUrl(fileUrl);
        fileCategoryData.setFileCategoryId(fileCategoryVo.getNewDirId());
        fileCategoryDataMapper.updateById(fileCategoryData);
    }

    /**
     * 查询指定用户的文件目录
     *
     * @param fileDataVo
     * @return
     */
    @Override
    public List<FileCategory> selectFileDir(FileCategoryVo fileDataVo) {
        FileCategory fileCategory = getFileDir(fileDataVo);
        LambdaQueryWrapper<FileCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(FileCategory::getParentDir, fileCategory.getId());
        childWrapper.orderByDesc(FileCategory::getCreateTime);
        return fileCategoryMapper.selectList(childWrapper);
    }

    /**
     * 查询文件列表
     *
     * @param fileDataVo
     * @return
     * @throws ServiceException
     */
    @Override
    public List<FileCategoryData> selectFile(FileCategoryVo fileDataVo) throws ServiceException {
        FileCategory fileCategory = getFileDir(fileDataVo);
        LambdaQueryWrapper<FileCategoryData> dateWrapper = new LambdaQueryWrapper<>();
        dateWrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
        dateWrapper.orderByDesc(FileCategoryData::getId);
        List<FileCategoryData> fileList = fileCategoryDataMapper.selectList(dateWrapper);
        for (FileCategoryData fileCategoryData : fileList) {
            fileCategoryData.setFileUrl(authFile(fileCategoryData.getFileUrl()));
        }
        return fileList;
    }

    /**
     * 查询指定目录
     *
     * @param fileDataVo
     * @return
     */
    private FileCategory getFileDir(FileCategoryVo fileDataVo) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(fileDataVo.getDirPath())) {
            wrapper.eq(FileCategory::getDirPath, "/" + userId + fileDataVo.getDirPath());
        } else {
            wrapper.eq(FileCategory::getDirPath, "/" + userId);
        }
        wrapper.eq(FileCategory::getUserId, userId);
        return fileCategoryMapper.selectOne(wrapper);
    }

    /**
     * 私有目录下文件授权
     *
     * @param path
     * @return
     * @throws ServiceException
     */
    public String authFile(String path) throws ServiceException {
        String searchStr = "/blog";
        int index = path.indexOf(searchStr);
        if (index != -1) {
            String fileUrl = path.substring(index + searchStr.length() + 1);
            return minioService.authFile(fileUrl, 3);
        } else {
            return "";
        }
    }

    /**
     * 同步单个文件至远程服务器
     *
     * @param fileDataVo
     * @return
     */
    @Override
    public void syncFile(FileCategoryDataVo fileDataVo) throws ServiceException {
        Integer fileId = fileDataVo.getId();
        FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(fileId);
        if (fileCategoryData == null) {
            throw new ServiceException("文件不存在");
        }
        Integer fileStatus = fileCategoryData.getFileStatus();
        Integer operateFileStatus = fileDataVo.getSyncFileStatus();
        if (fileStatus.equals(Constant.FILE_STATUS_WAIT)) {
            throw new ServiceException("文件正在等待同步");
        } else if (fileStatus.equals(Constant.FILE_STATUS_TO_LOCAL) || fileStatus.equals(Constant.FILE_STATUS_TO_REMOTE)) {
            throw new ServiceException("文件正在同步中");
        } else if (fileStatus.equals(Constant.FILE_STATUS_LOCAL)) {
            if (!operateFileStatus.equals(Constant.FILE_STATUS_REMOTE)) {
                throw new ServiceException("本地文件同步参数异常");
            }
        } else if (fileStatus.equals(Constant.FILE_STATUS_REMOTE)) {
            if (!operateFileStatus.equals(Constant.FILE_STATUS_LOCAL)) {
                throw new ServiceException("远程文件同步参数异常");
            }
        }

        Integer userId = SecurityUtil.getLoginUser().getId();
        FileCategory category = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());
        if (operateFileStatus.equals(Constant.FILE_STATUS_LOCAL)) {
            // 文件下载到本地
            // 文件存储minio中路径
            String minioPath = category.getDirPath();
            // servicePath 为文件在ftp system用户目录下的相对路径
            String servicePath = "/temp/" + MyStringUtils.getRandomString(6);
            // devicePath 为树莓派设备上的绝对路径
            String devicePath = "/mnt/test";
            // 同步文件名称
            String fileName = fileCategoryData.getFileName();

            // 发送netty消息
            NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToService(minioPath, servicePath, devicePath);
            nettySyncFileDto.setFileNameList(List.of(fileName));
            nettyFileSyncService.syncFileSend(null, nettySyncFileDto, userId);
        } else if (operateFileStatus.equals(Constant.FILE_STATUS_REMOTE)) {
            // 文件同步到远程
            String exportPath = Constant.FTP_PATH_SYSTEM_TEMP + "/" + MyStringUtils.getRandomString(6);
            String fileUrl = fileCategoryData.getFileUrl();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            minioService.exportFile(category.getDirPath() + "/" + fileName, exportPath);

            // 此处将文件在ftp的全路径转换为在ftp/system用户目录下的路径
            String serviceFilePath = exportPath.substring(Constant.FTP_PATH_SYSTEM.length());
            String deviceFilePath = "/opt/docker/files/temp";

            // 发送netty消息
            NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
            nettySyncFileDto.setFileNameList(List.of(fileName));
            nettyFileSyncService.syncFileSend(null, nettySyncFileDto, userId);
        }
    }

    /**
     * 文件导入minio
     *
     * @param nettyUploadBlogFileDto
     */
    @Override
    public void fileImportMinio(NettyFileSyncDto nettyUploadBlogFileDto, MsgHead msgHead) {
        logger.info("===== 文件导入minio ===== NettyFileSyncDto: {} ", nettyUploadBlogFileDto);
        Integer userId = msgHead.getUserId();
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        Integer categoryId = createDirWithUserId(minioPath);
        List<FileCategoryData> fileCategoryDataList = new ArrayList<>();
        for (String fileName : nettyUploadBlogFileDto.getFileNameList()) {

            // 文件本地存放目录
            String localFilePath = Constant.FTP_PATH_SYSTEM + nettyUploadBlogFileDto.getServiceFilePath() + "/" + fileName;

            // 文件转为 MultipartFile
            File file = new File(localFilePath);
            MultipartFile multipartFile = FileMultipartFileConverter.fileToMultipartFile(file);

            String fileUrl = minioService.getFileUrl(minioPath, fileName);
            FileCategoryData fileCategoryData = new FileCategoryData();
            fileCategoryData.setUserId(userId);
            fileCategoryData.setFileName(fileName);
            fileCategoryData.setFileCategoryId(categoryId);
            fileCategoryData.setFileUrl(fileUrl);
            fileCategoryData.setFileSize((int) multipartFile.getSize());
            fileCategoryData.setFileStatus(0);
            fileCategoryData.setFileType(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
            fileCategoryData.setFileJson(VideoUtil.resolveVideo(multipartFile));
            fileCategoryData.setCreateBy("system");
            fileCategoryData.setCreateTime(new Date());

            minioService.importFile(localFilePath, minioPath);

            fileCategoryDataList.add(fileCategoryData);
        }

        if (CollectionUtils.isNotEmpty(fileCategoryDataList)) {
            fileCategoryDataMapper.insert(fileCategoryDataList);
        }
    }


}
