package com.blog.core.domain.file.dict;

import lombok.Data;

import java.util.Date;

/**
 * @Description 字典数据实体类
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@Data
public class DictData {

    /**
     * id
     */
    private Integer id;

    /**
     * 字典编码
     */
    private String dictTypeCode;

    /**
     * 字典数据名称
     */
    private String dictDataLabel;

    /**
     * 字典数据值
     */
    private String dictDataValue;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最近修改时间
     */
    private Date updateTime;

}
