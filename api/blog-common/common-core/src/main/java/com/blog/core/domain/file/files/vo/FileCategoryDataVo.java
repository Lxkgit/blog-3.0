package com.blog.core.domain.file.files.vo;

import com.blog.core.domain.file.files.entity.FileCategoryData;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 文件数据vo类
 * @Author lxk
 * @CreateTime 2025-04-13
 */

@Getter
@Setter
public class FileCategoryDataVo extends FileCategoryData {

    /**
     * 目录基础路径
     */
    @Pattern(message = "路径需要以/开始且只允许汉字、数字、字母、下划线", regexp = "^/(?:[a-zA-Z0-9_\\u4e00-\\u9fa5]+/)*[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")
    private String dirPath;

    /**
     * 文件名称
     */
    private String fileName;
}

