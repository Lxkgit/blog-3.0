package com.blog.pi.domain.entity;

import lombok.Data;

import java.util.Date;

@Data
public class FileMD5 {

    private Integer id;

    private String fileMD5;

    private Integer fileCount;

    private Date createTime;

}
