package com.blog.core.domain.file.dict;

import lombok.Data;

import java.util.Date;

/**
 * @Description 字典目录实体类
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@Data
public class DictType {

    /**
     * id
     */
    private Integer id;

    /**
     * 字典名称
     */
    private String dictTypeName;

    /**
     * 字典编码
     */
    private String dictTypeCode;

    /**
     * 字典状态
     */
    private Integer dictTypeStatus;

    /**
     * 字典备注
     */
    private String dictTypeRemark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最近修改时间
     */
    private Date updateTime;


}
