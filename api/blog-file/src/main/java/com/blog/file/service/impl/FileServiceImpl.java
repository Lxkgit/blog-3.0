package com.blog.file.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.common.NettyConstant;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncBlogFileDto;
import com.blog.file.netty.domain.dto.file.NettyUploadBlogFileDto;
import com.blog.file.netty.domain.enums.NettyTopicEnum;
import com.blog.file.netty.service.NettyServer;
import com.blog.file.service.FileService;
import com.blog.file.service.UploadFileService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * @description: 文件服务
 * @Author: lxk
 * @date 2023/8/2 16:03
 */

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    @Resource
    private UploadFileService uploadFileService;

    @Resource
    private NettyServer nettyServer;

    @Resource
    private MinioService minioService;

    /**
     * 创建云盘目录
     *
     * @param fileCategoryVo
     * @throws ServiceException
     */
    @Override
    public void createDir(FileCategoryVo fileCategoryVo) throws ServiceException {
        if (StringUtils.isEmpty(fileCategoryVo.getDirPath())) {
            createDir("/" + fileCategoryVo.getDirName());
        } else {
            createDir(fileCategoryVo.getDirPath() + "/" + fileCategoryVo.getDirName());
        }
    }

    /**
     * 创建目录
     *
     * @param path 目录
     */
    public void createDir(String path) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String createDir = "/" + userId + path;
        uploadFileService.createFileCategory(createDir);
    }

    /**
     * 创建目录
     *
     * @param path 目录
     */
    public Integer createDirWithUserId(String path) {
        SecurityUtil.setSystem();
        return uploadFileService.createFileCategory(path);
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
     * @param fileCategoryData
     * @throws ServiceException
     */
    @Override
    public void deleteFile(FileCategoryDataVo fileCategoryData) throws ServiceException {
        uploadFileService.deleteFile(fileCategoryData);
        LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileCategoryData::getId, fileCategoryData.getId());
        wrapper.eq(FileCategoryData::getUserId, SecurityUtil.getLoginUser().getId());
        fileCategoryDataMapper.delete(wrapper);
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
            return minioService.authFile(fileUrl, 60);
        } else {
            return "";
        }
    }


//    /**
//     * 同步文件
//     *
//
//     * @param fileDataVoList
//     * @return
//     */
//    @Override
//    public boolean syncFileList(List<FileCategoryDataVo> fileDataVoList) {
//        for (FileCategoryDataVo fileData : fileDataVoList) {
//            if (fileData.getType().equals(Constant.FILE_TYPE_DIR)) {
//                // 目录不同步
//                continue;
//            }
//            if (fileData.getDirType().equals(Constant.DIR_TYPE_LOCAL)) {
//                // 本地目录不同步
//                continue;
//            }
//
//
//        }
//        return false;
//    }
//

    /**
     * 同步单个文件至远程服务器
     *
     * @param fileDataVo
     * @return
     */
    @Override
    public boolean syncFile(FileCategoryDataVo fileDataVo) throws ServiceException {
        Integer fileId = fileDataVo.getId();
        FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(fileId);
        if (fileCategoryData == null) {
            throw new ServiceException("文件不存在");
        }
        Integer fileStatus = fileCategoryData.getFileStatus();
        Integer operateFileStatus = fileDataVo.getFileStatus();
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

        FileCategory category = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());

        NettySyncBlogFileDto nettySyncBlogFile = new NettySyncBlogFileDto();
        nettySyncBlogFile.setFilePath(category.getDirPath());
        nettySyncBlogFile.setFileName(fileCategoryData.getFileName());
        if (operateFileStatus.equals(Constant.FILE_STATUS_LOCAL)) {
            nettySyncBlogFile.setSyncType(1);
        }
        if (operateFileStatus.equals(Constant.FILE_STATUS_REMOTE)) {
            nettySyncBlogFile.setSyncType(0);
        }

        if (nettySyncBlogFile.getSyncType().equals(0)) {
            // 文件同步至远程服务器，生成随机文件唯一编码
            String fileCode = UUID.randomUUID().toString();
            nettySyncBlogFile.setFileCode(fileCode);
            fileCategoryData.setFileCode(fileCode);
            fileCategoryDataMapper.updateFileCodeByIdAndUserId(fileCategoryData);
        } else if (nettySyncBlogFile.getSyncType().equals(1)) {
            // 文件同步至本地服务器，获取文件唯一编码
            nettySyncBlogFile.setFileCode(fileCategoryData.getFileCode());
        }
        nettySyncBlogFile.setUserId(SecurityUtil.getLoginUser().getId());
        NettyPacket<NettySyncBlogFileDto> syncFileRequest = NettyPacket.buildRequest(NettyTopicEnum.BLOG_FILE_SYNC.getTopic(), nettySyncBlogFile);
        nettyServer.channelWriteByRegisterId(NettyConstant.NETTY_CLIENT1, JSONObject.toJSONString(syncFileRequest), true);
        return true;
    }

    @Override
    public void fileImportMinio(NettyUploadBlogFileDto nettyUploadBlogFileDto) {
        Integer userId = nettyUploadBlogFileDto.getUserId();
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        Integer categoryId = createDirWithUserId(minioPath);
        List<FileCategoryData> fileCategoryDataList = new ArrayList<>();
        for (String fileName : nettyUploadBlogFileDto.getFileNameList()) {

            minioService.importFile(Constant.FTP_PATH_SYSTEM + nettyUploadBlogFileDto.getServiceFilePath() + "/" + fileName, minioPath);

            String fileUrl = minioService.getFileUrl(minioPath, fileName);
            FileCategoryData fileCategoryData = new FileCategoryData();
            fileCategoryData.setUserId(userId);
            fileCategoryData.setFileName(fileName);
            fileCategoryData.setFileCategoryId(categoryId);
            fileCategoryData.setFileUrl(fileUrl);
            fileCategoryData.setFileSize(0);
            fileCategoryData.setFileStatus(0);
            fileCategoryData.setFileType(fileName.substring(fileName.lastIndexOf(".")));
            fileCategoryData.setCreateBy("userName");
            fileCategoryData.setCreateTime(new Date());

            fileCategoryDataList.add(fileCategoryData);
        }

        if (CollectionUtils.isNotEmpty(fileCategoryDataList)) {
            fileCategoryDataMapper.insert(fileCategoryDataList);
        }
    }

//
//
//    /**
//     * 查询用户的文件目录
//     * @param path 文件目录
//     * @param userId 用户ID
//     * @return 文件目录下数据列表
//     */
//    private List<FileCategoryDataVo> show(String path, Integer userId) {
//        FileCategoryData dir = fileDataMapper.selectByPathAndName(path);
//        // 获取数据库中当前目录下文件列表
//        QueryWrapper<FileCategoryData> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("path", path);
//        List<FileCategoryData> fileDataList = fileDataMapper.selectList(queryWrapper);
//        List<FileCategoryDataVo> fileDataVoList = new ArrayList<>();
//        for (FileCategoryData fileData : fileDataList) {
//            FileCategoryDataVo fileDataVo = new FileCategoryDataVo();
//            BeanUtils.copyProperties(fileData, fileDataVo);
//            fileDataVoList.add(fileDataVo);
//        }
//        // 获取当前目录下实际文件列表
//        File[] files = (new File(path)).listFiles();
//        if (null != files && files.length > 0) {
//            for (File file : files) {
//                String fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
//                AtomicReference<Boolean> flag = new AtomicReference<>(false);
//                // 统计目录下文件大小
//                fileDataVoList.forEach(fileDataVo -> {
//                    if (fileDataVo.getName().toLowerCase().equals(file.getName().toLowerCase())) {
//                        flag.set(true);
//                        fileDataVo.setFlag(true);
//                        fileDataVo.setUpdateTime(new Date(file.lastModified()));
//
//                        if (!file.isFile()) {
//                            // 目录计算目录占用大小
//                            fileDataVo.setFileSize(FileUtils.sizeOf(file));
//                        } else {
//                            if (FileTypeEnum.IMAGE.getTypeSet().contains(fileType)) {
//                                // 图片添加图片链接
//                                fileDataVo.setImgPath(serviceIp + baseUri + path.substring(basePath.length()) + "/" + file.getName());
//                            }
//                            // 文件计算文件大小
//                            fileDataVo.setFileSize(file.length());
//                        }
//                    }
//                });
//
//                // 数据库中不存在的目录 文件中存在 将数据入库
//                if (!flag.get()) {
//                    FileCategoryDataVo fileDataVo = new FileCategoryDataVo();
//                    fileDataVo.setName(file.getName());
//                    fileDataVo.setPath(path);
//                    fileDataVo.setUserId(userId);
//                    fileDataVo.setDirType(dir == null ? 0 : dir.getDirType());
//                    fileDataVo.setStatus(Constant.FILE_TYPE_FILE);
//                    fileDataVo.setUpdateTime(new Date(file.lastModified()));
//                    if (!file.isFile()) {
//                        fileDataVo.setType(Constant.FILE_TYPE_DIR);
//                        fileDataVo.setFileSize(FileUtils.sizeOf(file));
//                    } else {
//                        if (FileTypeEnum.IMAGE.getTypeSet().contains(fileType)) {
//                            fileDataVo.setType(Constant.FILE_TYPE_FILE);
//                            fileDataVo.setImgPath(serviceIp + baseUri + path.substring(basePath.length()) + "/" + file.getName());
//                        } else {
//                            fileDataVo.setType(Constant.FILE_TYPE_IMAGE);
//                        }
//                        fileDataVo.setFileSize(file.length());
//                    }
//                    fileDataMapper.insert(fileDataVo);
//                    fileDataVo.setFlag(true);
//                    fileDataVoList.add(fileDataVo);
//                }
//            }
//            // 删除数据库中存在 文件目录中不存在的数据 (远程同步文件除外)
//            fileDataVoList.forEach(fileDataVo -> {
//                if (!fileDataVo.isFlag()) {
//                    // 删除该目录下全部数据
//                    QueryWrapper<FileCategoryData> wrapper = new QueryWrapper<>();
//                    wrapper.likeRight("path", path + "/" + fileDataVo.getName());
//                    wrapper.ne("dir_type", Constant.DIR_TYPE_SYNC);
//                    fileDataMapper.delete(wrapper);
//                    // 删除该目录
//                    if (!fileDataVo.getDirType().equals(Constant.DIR_TYPE_SYNC)) {
//                        fileDataMapper.deleteById(fileDataVo.getId());
//                        // 标记为false移除List中当前数据
//                        fileDataVo.setFlag(false);
//                    }
//                    fileDataVo.setFlag(true);
//                }
//            });
//            fileDataVoList.removeIf(fileDataVo -> !fileDataVo.isFlag());
//        } else {
//            // 文件目录为空 删除数据库中此目录下全部数据
//            QueryWrapper<FileCategoryData> wrapper = new QueryWrapper<>();
//            wrapper.likeRight("path", path);
//            wrapper.ne("dir_type", Constant.DIR_TYPE_SYNC);
//            fileDataMapper.delete(wrapper);
//            fileDataVoList.clear();
//        }
//        fileDataVoList.forEach(fileDataVo -> {
//            if (fileDataVo.getId() != null) {
//                fileDataMapper.updateById(fileDataVo);
//            }
//        });
//        Collections.sort(fileDataVoList);
//        return fileDataVoList;
//    }


}
