package com.blog.core.domain.file.dict.vo;

import com.blog.core.domain.file.dict.DictData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class DictDataVo extends DictData {

    private Integer pageNum;

    private Integer pageSize;
}
