package com.blog.file.service.impl;


import com.blog.core.domain.content.diary.entity.Diary;
import com.blog.core.domain.file.files.entity.FileUploadLog;
import com.blog.core.domain.file.files.vo.ImportDiaryVo;
import com.blog.core.utils.DateUtil;
import com.blog.core.utils.FileUtil;
import com.blog.core.utils.ZipFileUtil;
import com.blog.file.feign.ContentClient;
import com.blog.file.mapper.FileUploadLogMapper;
import com.blog.file.service.ImportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: lxk
 * @date 2023/2/1 15:20
 * @description: 数据导入服务类
 */

@Service
public class ImportServiceImpl implements ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Value("${file.basePath}")
    private String basePath;

    @Value("${file.serviceIp}")
    private String serviceIp;

    @Value("${file.baseUri}")
    private String baseUri;

    @Resource
    private FileUploadLogMapper uploadLogMapper;

    @Resource
    private ContentClient contentClient;

    @Override
    public boolean importDiary(ImportDiaryVo importDiaryVo) {
        boolean flag = false;
        try {
            String filePath = basePath + importDiaryVo.getFilePath().substring(serviceIp.length() + baseUri.length());
            System.out.println(filePath);
            File zipFile = new File(filePath);
            if (!zipFile.exists()) {
                System.out.println("文件不存在 ... ");
            }
            String descPath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + RandomStringUtils.randomAlphabetic(5);
            ZipFileUtil.unZipToFold(filePath, descPath);
            File diaryDir = new File(descPath);
            File[] files = diaryDir.listFiles();
            if (files != null) {
                flag = uploadDiary(files, importDiaryVo.getYear(), importDiaryVo.getUserId());
            }
            FileUtil.deleteDir(descPath);
            FileUtil.deleteFile(filePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return flag;
    }


    private boolean uploadDiary(File[] files, Integer year, Integer userId) {
        Map<String, Diary> map = new HashMap<>();
        for (File file : files) {
            String[] fileName = Objects.requireNonNull(file.getName()).split("\\.");
            String pattern = "^[0-9]{1,2}-[0-9]{1,2}$";
            boolean isMatch = Pattern.matches(pattern, fileName[0]);
            if (isMatch) {
                Diary diary = new Diary();
                diary.setUpdateTime(DateUtil.timeStampToDateTime(file.lastModified()));
                diary.setCreateTime(DateUtil.timeStampToDateTime(file.lastModified()));
                diary.setUserId(userId);
                String[] diaryDate = fileName[0].split("-");
                int month = Integer.parseInt(diaryDate[0]);
                int day = Integer.parseInt(diaryDate[1]);
                Calendar calendar = new GregorianCalendar(year, month - 1, day);
                diary.setDiaryDate(calendar.getTime());
                StringBuilder result = new StringBuilder();
                try {
                    //构造一个BufferedReader类来读取文件
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String s;
                    //使用readLine方法，一次读一行
                    while ((s = br.readLine()) != null) {
                        result.append(s);
                    }
                    br.close();
                } catch (Exception e) {
                    logger.error("读取文件错误{}", e.getMessage(), e);
                }
                diary.setDiaryMd(result.toString());
                map.put(DateUtil.formatDate(diary.getDiaryDate())+".txt", diary);
            }
        }
        Map<String, List<String>> result = contentClient.saveDiaryList(map);
        boolean flag = true;
        for (String key : result.keySet()) {
            List<String> resultList = result.get(key);
            for (String diaryDate : resultList) {
                int uploadState;
                String uploadStr;
                if ("save".equals(key) || "update".equals(key)) {
                    uploadState = 1;
                    uploadStr = "日记上传成功";
                } else {
                    flag = false;
                    uploadState = 2;
                    uploadStr = "日记上传失败";
                }
                logger.info("日记名称： {}", DateUtil.dateToDateTime(diaryDate) );
                FileUploadLog uploadLog = new FileUploadLog(userId, DateUtil.dateToDateTime(diaryDate), "diary", uploadState, uploadStr, new Date());
                uploadLogMapper.insert(uploadLog);
            }
        }
        return flag;
    }
}
