package com.blog.core.domain.content.article.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: lxk
 * @date 2022/6/8 19:48
 * @description: 文章实体类
 */

@Data
@TableName("article")
public class Article {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String title;

    private String contentMd;

    private String contentImg;

    private String contentMemo;

    private String articleType;

    private String articleLabel;

    private Integer articleStatus;

    private Integer browseCount;

    private Integer likeCount;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
