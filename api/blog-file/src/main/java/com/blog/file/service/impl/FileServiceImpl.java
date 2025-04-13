package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.netty.service.NettyServer;
import com.blog.file.service.FileService;
import com.blog.file.service.UploadFileService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description: 文件服务
 * @Author: lxk
 * @date 2023/8/2 16:03
 */

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;


    @Resource
    private UploadFileService uploadFileService;

    @Resource
    private NettyServer nettyServer;


    @Override
    public void saveFileDir(FileCategoryVo fileCategoryVo) throws ServiceException {
        uploadFileService.createDir(fileCategoryVo.getDirPath() + "/" + fileCategoryVo.getDirName());
    }

    @Override
    public void deleteFileDir(FileCategoryVo fileCategoryVo) throws ServiceException {

    }
//
//    private void deleteDir(File dir) {
//        File[] files = dir.listFiles();
//        // 删除dir 里面的内容
//        // 用到递归  此处注意不要经常用  因为java删除的内容是 在回收站找不到
//        for (File file : files) {
//            if (file.isFile()) {
//                file.delete();
//            } else {
//                deleteDir(file);
//            }
//        }
//        // 删除 dir
//        dir.delete();
//    }
//
//    @Override
//    public void updateFileOrDirName(FileCategoryDataVo fileDataVo) throws ServiceException {
//        if (fileDataVo.getName() == null || fileDataVo.getName().equals("")) {
//            throw new ServiceException(ErrorMessage.FILE_NAME_NULL_ERROR);
//        }
//        if (fileDataVo.getFilePath().equals("/")) {
//            throw new ServiceException(ErrorMessage.BASE_FILE_NOT_RENAME);
//        }
//        String path = basePath + "/" + 1 + fileDataVo.getFilePath();
//        new File(path + "/" + fileDataVo.getName()).renameTo(new File(path + "/" + fileDataVo.getRename()));
//
//        QueryWrapper<FileCategoryData> wrapper = new QueryWrapper<>();
//        wrapper.likeRight("path", path + "/" + fileDataVo.getName());
//        fileDataMapper.delete(wrapper);
//    }
//
    /**
     * 查询指定用户的文件目录
     *
     * @param fileDataVo
     * @return
     */
    @Override
    public List<FileCategory> selectFileDir(FileCategoryVo fileDataVo) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(fileDataVo.getDirPath())) {
            wrapper.eq(FileCategory::getDirPath, "/" + userId + fileDataVo.getDirPath());
        } else {
            wrapper.eq(FileCategory::getDirPath, "/" + userId);
        }
        wrapper.eq(FileCategory::getUserId, userId);
        FileCategory fileCategory = fileCategoryMapper.selectOne(wrapper);

        LambdaQueryWrapper<FileCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(FileCategory::getParentDir, fileCategory.getId());

        return fileCategoryMapper.selectList(childWrapper);
    }

    @Override
    public List<FileCategoryData> selectFile(FileCategoryVo fileDataVo) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(fileDataVo.getDirPath())) {
            wrapper.eq(FileCategory::getDirPath, "/" + userId + fileDataVo.getDirPath());
        } else {
            wrapper.eq(FileCategory::getDirPath, "/" + userId);
        }
        wrapper.eq(FileCategory::getUserId, userId);
        FileCategory fileCategory = fileCategoryMapper.selectOne(wrapper);

        LambdaQueryWrapper<FileCategoryData> dateWrapper = new LambdaQueryWrapper<>();
        dateWrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
        fileCategoryDataMapper.selectList(dateWrapper);
        return fileCategoryDataMapper.selectList(dateWrapper);
    }

    //
//    @Override
//    public Long selectUserSpace() {
//        QueryWrapper<FileCategoryData> wrapper = new QueryWrapper<>();
//        wrapper.eq("user_id", 1);
//        List<FileCategoryData> fileDataList = fileDataMapper.selectList(wrapper);
//        Long size = 0L;
//        for (FileCategoryData fileData : fileDataList) {
//            if (fileData.getFileSize() != null) {
//                size += fileData.getFileSize();
//            }
//        }
//        return size;
//    }
//
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
//    /**
//     * 同步单个文件至远程服务器
//     *
//
//     * @param fileDataVo
//     * @return
//     */
//    public boolean syncFile(FileCategoryDataVo fileDataVo) {
//        if (fileDataVo.getType().equals(Constant.FILE_TYPE_DIR)) {
//            // 目录不同步
//            return false;
//        }
//        if (fileDataVo.getDirType().equals(Constant.DIR_TYPE_LOCAL)) {
//            // 本地目录/文件不同步
//            return false;
//        }
//
//        NettySyncBlogFileDto nettySyncBlogFile = new NettySyncBlogFileDto();
//        nettySyncBlogFile.setFilePath(fileDataVo.getFilePath());
//        nettySyncBlogFile.setFileName(fileDataVo.getName());
//        nettySyncBlogFile.setSyncType(fileDataVo.getSyncType());
//        if (fileDataVo.getSyncType().equals(0)) {
//            String fileCode = UUID.randomUUID().toString();
//            nettySyncBlogFile.setFileCode(fileCode);
//            fileDataVo.setFileCode(fileCode);
//            fileDataMapper.updateFileCodeByIdAndUserId(fileDataVo);
//        } else if (fileDataVo.getSyncType().equals(1)) {
//            FileCategoryData fileData = fileDataMapper.selectById(fileDataVo.getId());
//            nettySyncBlogFile.setFileCode(fileData.getFileCode());
//        }
//        NettyPacket<NettySyncBlogFileDto> syncFileRequest = NettyPacket.buildRequest(nettySyncBlogFile);
//        syncFileRequest.setTopic(NettyTopicEnum.BLOG_FILE_SYNC.getTopic());
//        nettyServer.channelWriteByRegisterId(NettyConstant.NETTY_CLIENT1, JSONObject.toJSONString(syncFileRequest));
//        return true;
//    }
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
