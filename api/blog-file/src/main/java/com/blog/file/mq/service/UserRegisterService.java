package com.blog.file.mq.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.domain.file.files.vo.FileCategoryVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.service.FileService;
import com.blog.mq.entity.MqMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @Description 用户注册服务处理
 * @Author lxk
 * @CreateTime 2026-08-14
 */

@Service
public class UserRegisterService {

    @Resource
    private FileService fileService;

    public void createDir(MqMessage mqMessage) throws ServiceException {
        User user = JSONObject.parseObject(mqMessage.getMessage(), User.class);
        SecurityUtil.setUser(user.getId(), user.getUsername());
        List<String> dirList = Arrays.asList("article", "doc", "diary", "device", "user", "other");
        for (String dir : dirList) {
            FileCategoryVo fileCategoryVo = new FileCategoryVo();
            fileCategoryVo.setDirName(dir);
            fileCategoryVo.setDirType(1);
            fileService.createDir(fileCategoryVo);
        }
    }
}
