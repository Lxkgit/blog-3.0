package com.blog.core.domain.file.files.vo;


import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.valication.annotation.Equal;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description:
 * @Author: lxk
 * @date 2023/8/2 16:08
 */


@Setter
@Getter
public class FileCategoryVo extends FileCategory {

    /**
     * 目录基础路径
     */
    @Pattern(message = "路径需要以/开始且只允许汉字、数字、字母、下划线", regexp = "^/(?:[a-zA-Z0-9_\\u4e00-\\u9fa5]+/)*[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")
    private String dirPath;

    /**
     * 目录名称
     */
    @Pattern(message = "目录名称只允许汉字、数字、字母、下划线", regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")
    private String dirName;

}
