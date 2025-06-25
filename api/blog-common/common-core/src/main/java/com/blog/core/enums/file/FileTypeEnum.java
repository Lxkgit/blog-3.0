package com.blog.core.enums.file;

import java.util.*;

/**
 * @Author: lxk
 * @date 2023/5/11 20:34
 * @description: 文件类型枚举类
 */

public enum FileTypeEnum {

    IMAGE(1, "图片", new HashSet<>(Arrays.asList("jpg", "png")), "/img"),
    FILE(2, "文件", new HashSet<>(Arrays.asList("zip", "7z")), "/file"),
    VIDEO(3, "视频", new HashSet<>(Arrays.asList("mp4", "avi")), "/video"),
    DIARY_FILE(4, "其他", new HashSet<>(), "/other");

    /**
     * 文件类型编码
     */
    private Integer fileType;

    /**
     * 文件类型名称
     */
    private String fileTypeName;

    /**
     * 文件后缀名
     */
    private Set<String> typeSet;

    /**
     * 文件类型存放位置
     */
    private String fileTypePath;

    public static FileTypeEnum getTypeEnumByFileType(String fileType) {
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.getTypeSet().contains(fileType)) {
                return fileTypeEnum;
            }
        }
        return DIARY_FILE;
    }

    public static String getTypePathByTypeName(Integer fileType) {
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.getFileType().equals(fileType)) {
                return fileTypeEnum.getFileTypePath();
            }
        }
        return null;
    }

    FileTypeEnum(Integer fileType, String fileTypeName, Set<String> typeSet, String fileTypePath) {
        this.fileType = fileType;
        this.fileTypeName = fileTypeName;
        this.typeSet = typeSet;
        this.fileTypePath = fileTypePath;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileTypeName() {
        return fileTypeName;
    }

    public void setFileTypeName(String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    public Set<String> getTypeSet() {
        return typeSet;
    }

    public void setTypeSet(Set<String> typeSet) {
        this.typeSet = typeSet;
    }

    public String getFileTypePath() {
        return fileTypePath;
    }

    public void setFileTypePath(String fileTypePath) {
        this.fileTypePath = fileTypePath;
    }
}
