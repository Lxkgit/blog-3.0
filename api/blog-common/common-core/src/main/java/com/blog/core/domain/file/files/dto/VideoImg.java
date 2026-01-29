package com.blog.core.domain.file.files.dto;

import lombok.Data;

/**
 * @Description: 视频封面数据类
 * @Author lxk
 * @CreateTime 2026-01-29
 */

@Data
public class VideoImg {

    /**
     * 视频文件id
     */
    private Integer id;

    /**
     * 视频base64封面图
     */
    private String videoImg;

    public VideoImg(Integer id, String videoImg) {
        this.id = id;
        this.videoImg = videoImg;
    }
}
