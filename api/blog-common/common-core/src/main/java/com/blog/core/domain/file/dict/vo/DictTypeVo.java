package com.blog.core.domain.file.dict.vo;

import com.blog.core.domain.file.dict.DictType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class DictTypeVo extends DictType {

    private Integer pageNum;

    private Integer pageSize;
}
