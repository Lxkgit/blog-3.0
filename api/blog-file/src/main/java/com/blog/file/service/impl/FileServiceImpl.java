package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.enums.file.FileTypeEnum;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.file.service.FileService;
import com.blog.file.service.UploadFileService;
import com.blog.file.utils.VideoUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;

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
    private NettySyncFileService nettyFileSyncService;

    @Resource
    private Executor baseThread;

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
            uploadFileService.deleteFile(idList);
            fileCategoryDataMapper.delete(wrapper);
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
                oldFileCategory.getDirPath() + "/" + newFileCategory.getDirName() + "/");

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
    public List<FileCategoryDataVo> selectFile(FileCategoryVo fileDataVo) throws Exception {
        FileCategory fileCategory = getFileDir(fileDataVo);
        LambdaQueryWrapper<FileCategoryData> dateWrapper = new LambdaQueryWrapper<>();
        dateWrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
        dateWrapper.orderByDesc(FileCategoryData::getId);
        List<FileCategoryData> fileList = fileCategoryDataMapper.selectList(dateWrapper);
        List<FileCategoryDataVo> fileVoList = new ArrayList<>();
        for (FileCategoryData fileCategoryData : fileList) {
            FileCategoryDataVo vo = new FileCategoryDataVo();
            BeanUtils.copyProperties(fileCategoryData, vo);
            vo.setFileUrl(authFile(fileCategoryData.getFileUrl()));
            // 视频文件生成封面缩略图
            if (FileTypeEnum.getTypeEnumByFileType(fileCategoryData.getFileType()).getFileType() == 3) {
                // 2. 抓取 6 张有效帧
                List<BufferedImage> frames = grabFrames(fileCategoryData.getFileUrl(), 8);

                // 3. 取前 4 张生成 2x2 封面
                List<BufferedImage> coverFrames = frames.subList(0, 4);
                BufferedImage cover = build2x2Cover(coverFrames, 360);

                // 4. 转 Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(cover, "jpg", baos);
                vo.setVideoImg(Base64.getEncoder().encodeToString(baos.toByteArray()));
            }
            fileVoList.add(vo);
        }
        return fileVoList;
    }

    /**
     * 抓取指定数量的有效帧（跳黑屏）
     */
    private static List<BufferedImage> grabFrames(String url, int frameCount) {
        List<BufferedImage> result = new ArrayList<>();
        try {
            Map<Double, BufferedImage> map = new TreeMap<>(Collections.reverseOrder());
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url)) {
                grabber.start();
                long duration = grabber.getLengthInTime();

                // 使用 try-with-resources 自动关闭 Java2DFrameConverter
                try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                    for (int i = 0; i < frameCount; i++) {
                        long ts = duration * (i + 1);
                        grabber.setTimestamp(ts);
                        Frame frame = grabber.grabImage();
                        if (frame == null) {
                            continue;
                        }

                        BufferedImage img = converter.getBufferedImage(frame);
                        map.put(isBlackFrame(img), img);
                    }
                }

                for (Map.Entry<Double, BufferedImage> entry : map.entrySet()) {
                    result.add(entry.getValue());
                }

                grabber.stop();
            }
        } catch (Exception e) {
            logger.error("抓取视频封面帧异常: {}", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 判断黑屏帧
     */
    private static Double isBlackFrame(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int stepX = Math.max(w / 50, 1);
        int stepY = Math.max(h / 50, 1);
        long sum = 0, count = 0;

        for (int x = 0; x < w; x += stepX) {
            for (int y = 0; y < h; y += stepY) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // 计算这个像素的亮度（灰度值）
                int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // 把所有采样像素的亮度累加起来
                sum += lum;
                // 统计采样的像素数量（因为不是每个像素都算，stepX/stepY 是跳步采样）
                count++;
            }
        }
        // 采样像素的平均亮度
        return (double) sum / count;
    }

    /**
     * 生成 2x2 封面
     */
    private static BufferedImage build2x2Cover(List<BufferedImage> images, int totalHeight) {
        if (images.size() != 4) {
            throw new IllegalArgumentException("需要 4 张帧生成封面");
        }

        int cellHeight = totalHeight / 2;
        int[] cellWidths = new int[4];
        for (int i = 0; i < 4; i++) {
            BufferedImage img = images.get(i);
            cellWidths[i] = img.getWidth() * cellHeight / img.getHeight();
        }
        int totalWidth = Math.max(Math.max(cellWidths[0], cellWidths[1]),
                Math.max(cellWidths[2], cellWidths[3]));

        BufferedImage canvas = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        drawCentered(g, images.get(0), 0, 0, totalWidth / 2, cellHeight);
        drawCentered(g, images.get(1), totalWidth / 2, 0, totalWidth / 2, cellHeight);
        drawCentered(g, images.get(2), 0, cellHeight, totalWidth / 2, cellHeight);
        drawCentered(g, images.get(3), totalWidth / 2, cellHeight, totalWidth / 2, cellHeight);

        g.dispose();
        return canvas;
    }

    private static void drawCentered(Graphics2D g, BufferedImage img, int x, int y, int boxW, int boxH) {
        double scale = Math.min((double) boxW / img.getWidth(), (double) boxH / img.getHeight());
        int w = (int) (img.getWidth() * scale);
        int h = (int) (img.getHeight() * scale);
        int dx = x + (boxW - w) / 2;
        int dy = y + (boxH - h) / 2;
        g.drawImage(img, dx, dy, w, h, null);
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
            String devicePath = Constant.DISK_PATH_BLOG_MINIO + category.getDirPath();
            // 同步文件名称
            String fileName = fileCategoryData.getFileName();

            // 发送netty消息
            NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToService(minioPath, servicePath, devicePath);
            nettySyncFileDto.setFileNameList(List.of(fileName));
            nettySyncFileDto.setFileCodeList(List.of(fileCategoryData.getId() + ":" + fileCategoryData.getFileName()));
            // 异步导出文件并发送请求
            baseThread.execute(() -> nettyFileSyncService.sendSyncFileMsg(null, nettySyncFileDto, userId));
            // 文件状态修改为正在同步远程服务器
            updateFileCategoryDataStatus(fileCategoryData.getId(), Constant.FILE_STATUS_TO_REMOTE);
        } else if (operateFileStatus.equals(Constant.FILE_STATUS_REMOTE)) {
            // 文件同步到远程
            String exportPath = Constant.FTP_PATH_SYSTEM_TEMP + "/" + MyStringUtils.getRandomString(6);
            String fileUrl = fileCategoryData.getFileUrl();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            minioService.exportFile(category.getDirPath() + "/" + fileName, exportPath);

            // 此处将文件在ftp的全路径转换为在ftp/system用户目录下的路径
            String serviceFilePath = exportPath.substring(Constant.FTP_PATH_SYSTEM.length());
            String deviceFilePath = Constant.DISK_PATH_BLOG_MINIO + category.getDirPath();

            // 发送netty消息
            NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);

            if (category.getDirPath().startsWith("^/\\d+/user.*")) {
                nettySyncFileDto.setMinioDeleteFlag(1);
            } else {
                nettySyncFileDto.setMinioDeleteFlag(0);
            }
            nettySyncFileDto.setMinioPath(category.getDirPath());
            nettySyncFileDto.setFileNameList(List.of(fileName));
            nettySyncFileDto.setFileCodeList(List.of(fileCategoryData.getId() + ":" + fileCategoryData.getFileName()));
            // 异步发送上传文件命令
            baseThread.execute(() -> nettyFileSyncService.sendSyncFileMsg(null, nettySyncFileDto, userId));
            // 文件状态修改为正在同步本地服务器
            updateFileCategoryDataStatus(fileCategoryData.getId(), Constant.FILE_STATUS_TO_LOCAL);
        }
    }

    /**
     * 根据id修改文件状态
     *
     * @param id
     * @param fileStatus
     */
    private void updateFileCategoryDataStatus(Integer id, Integer fileStatus) {
        FileCategoryData update = new FileCategoryData();
        update.setId(id);
        update.setFileStatus(fileStatus);
        fileCategoryDataMapper.updateById(update);
    }

    /**
     * 文件导入minio
     *
     * @param nettyUploadBlogFileDto
     */
    @Override
    public void fileImportMinio(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead) {
        logger.info("===== 文件导入minio ===== NettySyncFileDto: {} ", nettyUploadBlogFileDto);
        Integer userId = msgHead.getUserId();
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        if (StringUtils.isEmpty(minioPath)) {
            return;
        }
        Integer categoryId = createDirWithUserId(minioPath);
        for (String fileName : nettyUploadBlogFileDto.getFileNameList()) {

            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getFileCategoryId, categoryId);
            wrapper.eq(FileCategoryData::getFileName, fileName);
            FileCategoryData fileCategoryData = fileCategoryDataMapper.selectOne(wrapper);

            if (fileCategoryData == null) {
                // 文件本地存放目录
                String localFilePath = Constant.FTP_PATH_SYSTEM + nettyUploadBlogFileDto.getServiceFilePath() + "/" + fileName;

                // 文件转为 MultipartFile
                File file = new File(localFilePath);

                String fileUrl = minioService.getFileUrl(minioPath, fileName);
                FileCategoryData newFile = new FileCategoryData();
                newFile.setUserId(userId);
                newFile.setFileName(fileName);
                newFile.setFileCategoryId(categoryId);
                newFile.setFileUrl(fileUrl);
                newFile.setFileSize(file.length());
                newFile.setFileStatus(0);
                newFile.setFileType(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
                newFile.setFileJson(VideoUtil.resolveVideo(file));
                newFile.setCreateBy("system");
                newFile.setCreateTime(new Date());

                boolean importFlag = minioService.importFile(localFilePath, minioPath);
                logger.info("系统外部文件导入minio结果: {}", importFlag);
                fileCategoryDataMapper.insert(newFile);

            } else {
                // 文件本地存放目录
                String localFilePath = Constant.FTP_PATH_SYSTEM + nettyUploadBlogFileDto.getServiceFilePath() + "/" + fileName;
                boolean importFlag = minioService.importFile(localFilePath, minioPath);
                logger.info("系统内部文件导入minio结果: {}", importFlag);
                fileCategoryData.setFileStatus(0);
                fileCategoryDataMapper.updateById(fileCategoryData);
            }
        }
    }

    /**
     * 文件下载到树莓派设备
     *
     * @param nettyUploadBlogFileDto
     * @param msgHead
     */
    @Override
    public void fileDownloadDevice(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead) {
        logger.info("树莓派下载完成 {} 文件", nettyUploadBlogFileDto.getFileNameList());
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        if (StringUtils.isEmpty(minioPath)) {
            return;
        }
        Integer categoryId = createDirWithUserId(minioPath);
        for (String fileName : nettyUploadBlogFileDto.getFileNameList()) {

            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getFileCategoryId, categoryId);
            wrapper.eq(FileCategoryData::getFileName, fileName);
            FileCategoryData fileCategoryData = fileCategoryDataMapper.selectOne(wrapper);

            if (fileCategoryData != null) {
                fileCategoryData.setFileStatus(4);
                fileCategoryDataMapper.updateById(fileCategoryData);
            }
        }

        if (nettyUploadBlogFileDto.getMinioDeleteFlag() == 1) {
            if (CollectionUtils.isNotEmpty(nettyUploadBlogFileDto.getFileCodeList())) {
                for (String fileCode : nettyUploadBlogFileDto.getFileCodeList()) {
                    Integer id = Integer.valueOf(fileCode.split(":")[0]);
                    FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(id);
                    FileCategory fileCategory = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());

                    // 修改目录下文件状态为远程服务器
                    LambdaQueryWrapper<FileCategoryData> dataWrapper = new LambdaQueryWrapper<>();
                    dataWrapper.eq(FileCategoryData::getFileCategoryId, fileCode);
                    FileCategoryData data = new FileCategoryData();
                    data.setFileStatus(4);
                    fileCategoryDataMapper.update(data, dataWrapper);

                    // 移除minio中文件
                    String fileName = minioService.getFileName(fileCategoryData.getFileUrl());
                    minioService.deleteFile(fileCategory.getDirPath(), fileName);
                }
            }
        }
    }
}
