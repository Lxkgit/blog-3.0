package com.blog.pi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Data
@TableName("file_MD5")
public class FileMD5 {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("file_md5")
    private String fileMD5;

    private Integer fileCount;

    private Date createTime;

}
