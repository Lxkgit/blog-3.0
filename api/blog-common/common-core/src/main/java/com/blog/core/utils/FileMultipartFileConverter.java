package com.blog.core.utils;


import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @Description File 与 MultipartFile 互转工具类
 * @Author lxk
 * @CreateTime 2025-10-13
 */
public class FileMultipartFileConverter {

    /**
     * 将本地 File 转为 MultipartFile（自定义实现）
     */
    public static MultipartFile fileToMultipartFile(File file) {
        return new SimpleMultipartFile(file);
    }

    /**
     * 将 MultipartFile 转为 File（存到临时目录）
     */
    public static File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload_", "_" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    /**
     * 自定义 MultipartFile 实现
     */
    private static class SimpleMultipartFile implements MultipartFile {

        private final File file;
        private final String name;

        public SimpleMultipartFile(File file) {
            this.file = file;
            this.name = file.getName();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            // 可根据扩展名判断 MIME 类型
            String lower = name.toLowerCase();
            if (lower.endsWith(".mp4")) return "video/mp4";
            if (lower.endsWith(".avi")) return "video/x-msvideo";
            if (lower.endsWith(".mov")) return "video/quicktime";
            if (lower.endsWith(".mkv")) return "video/x-matroska";
            if (lower.endsWith(".flv")) return "video/x-flv";
            if (lower.endsWith(".wmv")) return "video/x-ms-wmv";
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }

        @Override
        public long getSize() {
            return file.length();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return java.nio.file.Files.readAllBytes(file.toPath());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            try (InputStream in = getInputStream(); OutputStream out = new FileOutputStream(dest)) {
                in.transferTo(out);
            }
        }
    }
}
