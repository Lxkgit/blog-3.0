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

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * md格式文章内容
     */
    private String contentMd;

    /**
     * 文章首页展示图片
     */
    private String contentImg;

    /**
     * 文章备注
     */
    private String contentMemo;

    /**
     * 文章附件链接
     */
    private String articleFile;

    /**
     * 文章类型
     */
    private String articleType;

    /**
     * 文章标签
     */
    private String articleLabel;

    /**
     * 文章状态 （2：置顶 1：发布 0：草稿）
     */
    private Integer articleStatus;

    /**
     * 文章浏览次数
     */
    private Integer browseCount;

    /**
     * 文章点赞次数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最近修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
