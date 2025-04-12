package com.blog.core.domain.file.files.vo;


import com.blog.core.enums.file.FilePathEnum;
import com.blog.core.enums.file.FileTypeEnum;
import com.blog.core.valication.annotation.EnumValidate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: lxk
 * @date: 2023/1/29 17:15
 * @description: 文件上传Vo
 * @modified By:
 */

@Data
public class UploadVo {

        /**
         * 上传文件数据
         */
        @NotNull(message="导入文件不能为空")
        private MultipartFile files;

        /**
         * 文件类型编码
         */
        @NotNull(message="文件类型编码不能为空")
        @EnumValidate(enumClass = FileTypeEnum.class, methodName = "getFileType", message = "文件类型编码错误")
        private Integer fileTypeCode;

        /**
         * 文件存放路径编码
         */
        @NotNull(message="文件存放路径编码不能为空")
        @EnumValidate(enumClass = FilePathEnum.class, methodName = "getFilePathCode", message = "文件存放路径编码错误")
        private Integer filePathCode;

        /**
         * 日记上传使用
         */
        private Integer year;

        /**
         * 文件上传补充路径
         */
        @Pattern(message = "路径需要以/开始且只允许汉字、数字、字母、下划线", regexp = "^/([a-zA-Z0-9_\\u4e00-\\u9fa5]+/?)+$")
        private String addPath;
}
