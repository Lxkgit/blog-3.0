package com.blog.file.service.impl;


/**
 * @Author: lxk
 * @date 2022/7/7 15:59
 * @description: 文件上传服务
 */

@Slf4j
@Service
public class UploadFileServiceImpl implements UploadFileService {

    @Resource
    private UploadLogDAO uploadLogDAO;

    @Resource
    private UploadFileDAO uploadImgDAO;

    @Value("${file.basePath}")
    private String basePath;

    @Value("${file.serviceIp}")
    private String serviceIp;

    @Value("${file.baseUri}")
    private String baseUri;

    @Override
    public Result upload(MultipartFile[] files, Integer userId, String filePath) {
        Date date = new Date();
        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (fileName != null && !fileName.equals("")) {
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                String formatDate = DateUtil.formatDateTime(date).replace(" ", "_").replace(":", "-");
                String newFileName = formatDate + "_" + MyStringUtils.getRandomString(6) + "_" + fileName;
                UploadLog uploadLog = new UploadLog(userId, newFileName, fileType, 0, "", date);
                uploadLogDAO.insert(uploadLog);
                try {
                    File targetFile;
                    File file1 = new File(basePath + filePath);
                    if (!file1.exists() && !file1.isDirectory()) {
                        file1.mkdirs();
                    }
                    targetFile = new File(file1, newFileName);
                    file.transferTo(targetFile);
                    String url = serviceIp + baseUri + filePath + "/" + newFileName;

                    result.add(url);
                    uploadImgDAO.insert(new UploadFile(userId, newFileName, url, date, fileType, basePath + filePath));
                    uploadLogDAO.updateById(new UploadLog(uploadLog.getId(), userId, 1, "文件上传成功"));

                } catch (Exception e) {
                    uploadLogDAO.updateById(new UploadLog(uploadLog.getId(), userId, 2, "文件上传失败"));
                    log.error(e.getMessage(), e);
                }
            }
        }
        return ResultFactory.buildSuccessResult(result);
    }

}