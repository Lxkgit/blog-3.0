package com.blog.core.domain.content.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: lxk
 * @date 2022/6/20 9:56
 * @description: 文档内容
 */

@Data
@TableName("doc_content")
public class DocContent {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer catalogId;

    private String docContentMd;

    private Integer browseCount;

    private Integer likeCount;

    // 文档状态 0:草稿 1:发布 2: 3: 删除
    private Integer docStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
