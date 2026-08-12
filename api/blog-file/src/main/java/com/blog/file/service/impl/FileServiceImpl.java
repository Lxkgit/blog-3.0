package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.files.vo.FileCategoryDataVo;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.file.netty.service.NettySyncFileSendService;
import com.blog.file.netty.service.NettySyncFileReceiveService;
import com.blog.file.service.FileService;
import com.blog.file.service.UploadFileService;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.service.RedisService;
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
    private NettySyncFileReceiveService nettyFileSyncService;

    @Resource
    private RedisService redisService;

    @Resource
    private Executor baseThread;

    @Resource
    private NettySyncFileSendService nettySyncFileSendService;


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
            fileVoList.add(vo);
//            // 视频文件生成封面缩略图
//            if (FileTypeEnum.getTypeEnumByFileType(fileCategoryData.getFileType()).getFileType() == 3 &&
//                    Constant.FILE_STATUS_LOCAL.equals(fileCategoryData.getFileStatus())) {
//                String redisKey = FileRedisConstant.FILE_VIDEO_BASE64_IMG + vo.getId();
//                // Redis 命中直接用
//                if (redisService.hasKey(redisKey)) {
//                    vo.setVideoImg(redisService.getStringAndRefresh(redisKey, 60 * 60 * 24 * 7));
//                    continue;
//                }
//                // 提交封面生成任务
//                baseThread.execute(() -> generateVideoCoverBase64(vo.getId(), vo.getFileUrl()));
//            }
        }

        return fileVoList;
    }

    /**
     * 多线程生成视频封面
     */
    private void generateVideoCoverBase64(Integer id, String fileUrl) {
        try {
            logger.info("开始获取视频封面 id: {}", id);
            String redisKey = FileRedisConstant.FILE_VIDEO_BASE64_IMG + id;
            List<BufferedImage> frames = grabFrames(id, fileUrl);
            if (CollectionUtils.isNotEmpty(frames)) {
                // 转 Base64
                ByteArrayOutputStream base64 = new ByteArrayOutputStream();
                ImageIO.write(buildSingleCover(frames.get(frames.size() / 2)), "jpg", base64);
                String base64Img = Constant.BASE64_IMG_JPG + Base64.getEncoder().encodeToString(base64.toByteArray());
                redisService.setString(redisKey, base64Img, 60 * 60 * 24 * 7);
                logger.info("视频封面 id: {} 获取成功", id);
            } else {
                logger.info("视频封面 id: {} 获取失败", id);
            }
        } catch (Exception e) {
            logger.error("生成视频封面失败，id: {}", id, e);
        }
    }


    /**
     * 生成单帧封面，按指定高度自适应宽度
     */
    private static BufferedImage buildSingleCover(BufferedImage image) {
        int totalHeight = 300;
        if (image == null) {
            throw new IllegalArgumentException("需要一张帧生成封面");
        }
        // 计算等比缩放宽度
        int width = image.getWidth() * totalHeight / image.getHeight();
        // 创建画布
        BufferedImage canvas = new BufferedImage(width, totalHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 绘制缩放后的图片
        g.drawImage(image, 0, 0, width, totalHeight, null);
        g.dispose();
        return canvas;
    }

    /**
     * 抓取视频封面帧（异步用，尽可能保证抓到帧）
     */
    private static List<BufferedImage> grabFrames(Integer id, String url) {
        // 只抓 1 帧就够封面
        int frameCount = 1;
        List<BufferedImage> result = new ArrayList<>();
        try {
            Map<Double, BufferedImage> map = new TreeMap<>(Collections.reverseOrder());
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
                 Java2DFrameConverter converter = new Java2DFrameConverter()) {
                // 网络视频优化选项
                // 5秒
                grabber.setOption("rw_timeout", "5000000");
                // 5秒
                grabber.setOption("stimeout", "5000000");
                grabber.setOption("reconnect", "1");
                grabber.setAudioChannels(0);
                grabber.setOption("threads", "1");
                grabber.start();
                long duration = grabber.getLengthInTime();
                // 多位置尝试抓帧，顺序: 20%, 33%, 50%, 视频开头
                long[] seekPositions = {duration / 5, duration / 3, duration / 2, 0};
                Frame frame;
                // 每个位置最大循环 60 秒
                long maxLoopTimeMs = 120_000;
                for (long step : seekPositions) {
                    grabber.setTimestamp(step);
                    long startTime = System.currentTimeMillis();

                    while ((frame = grabber.grab()) != null && map.size() < frameCount) {
                        // 时间兜底
                        if (System.currentTimeMillis() - startTime > maxLoopTimeMs) {
                            logger.warn("抓取视频帧超时，强制中断 id: {}", id);
                            break;
                        }
                        if (frame.image == null) {
                            continue;
                        }
                        BufferedImage img = converter.getBufferedImage(frame);
                        if (img == null) {
                            continue;
                        }
                        double brightness = calcBrightness(img);
                        // 防止亮度相同覆盖
                        map.put(brightness + Math.random() * 1e-6, img);
                    }
                    // 一旦抓到帧就结束
                    if (!map.isEmpty()) {
                        break;
                    }
                }
                grabber.stop();
            }
            result.addAll(map.values());
        } catch (Exception e) {
            logger.error("抓取视频封面帧异常 id: {}", id, e);
        }
        return result;
    }

    private static double calcBrightness(BufferedImage img) {
        long sum = 0;
        int count = 0;
        for (int y = 0; y < img.getHeight(); y += 10) {
            for (int x = 0; x < img.getWidth(); x += 10) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                sum += r + g + b;
                count++;
            }
        }
        return sum * 1.0 / count;
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
            nettySyncFileDto.setFileNameList(List.of(fileCategoryData.getId() + ":" + fileCategoryData.getFileName()));
            // 异步导出文件并发送请求
            baseThread.execute(() -> nettySyncFileSendService.sendSyncFileMsg(null, nettySyncFileDto, userId));
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
            nettySyncFileDto.setFileNameList(List.of(fileCategoryData.getId() + ":" + fileCategoryData.getFileName()));
            // 异步发送上传文件命令
            baseThread.execute(() -> nettySyncFileSendService.sendSyncFileMsg(null, nettySyncFileDto, userId));
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




}
