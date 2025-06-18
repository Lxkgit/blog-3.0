package com.blog.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @Author: lxk
 * @date 2023/2/1 16:26
 * @description: 文件操作工具类
 */

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if(file.exists() && file.isFile()) {
            if (file.delete()) {
                logger.info("文件删除成功: fileName: {}", fileName);
            } else {
                logger.error("文件删除失败: fileName: {}", fileName);
            }
        } else {
            logger.error("文件不存在: fileName: {}", fileName);
        }
    }

    public static void  deleteDir(String dir) {
        if (dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            logger.error("删除文件夹失败： {} 不存在", dir);
        }
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    deleteFile(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    deleteDir(file.getAbsolutePath());
                }
            }
        }
        if (dirFile.delete()) {
            logger.info("文件夹删除成功： {}", dir);
        }
    }
}
