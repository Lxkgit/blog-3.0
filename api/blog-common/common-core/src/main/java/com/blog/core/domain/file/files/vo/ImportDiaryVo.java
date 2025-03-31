package com.blog.core.domain.file.files.vo;

import lombok.Data;

/**
 * @Author: lxk
 * @date 2023/2/1 15:18
 * @description: 导入日记Vo
 */

@Data
public class ImportDiaryVo {

    Integer year;
    String filePath;
    Integer userId;

}
