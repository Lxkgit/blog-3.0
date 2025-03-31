package com.blog.core.domain.file.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: 博客用户内容数量统计表
 * @Author: lxk
 * @date 2023/6/28 17:23
 */
@Data
@TableName("content_count")
public class ContentCount {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private Integer docCount;
    private Integer articleCount;
    private Integer diaryCount;
}
