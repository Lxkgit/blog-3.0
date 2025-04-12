package com.blog.file.service;

import com.blog.core.domain.file.files.vo.UploadVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: lxk
 * @date: 2022/7/7 20:50
 * @description:
 * @modified By:
 */
public interface UploadFileService {

    String uploadService(UploadVo uploadVo);

}
